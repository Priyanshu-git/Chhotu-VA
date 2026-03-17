package com.nexxlabs.chhotu.domain.engine.ai

import android.util.Log
import com.google.gson.Gson
import com.nexxlabs.chhotu.BuildConfig
import com.nexxlabs.chhotu.data.remote.LLMService
import com.nexxlabs.chhotu.data.remote.model.ChatCompletionRequest
import com.nexxlabs.chhotu.data.remote.model.Message
import com.nexxlabs.chhotu.domain.engine.EngineInterface
import com.nexxlabs.chhotu.domain.engine.EngineUtil.fallbackIntent
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIIntentEngine
@Inject
constructor(
        private val service: LLMService,
        private val gson: Gson,
        private val appRegistry: AppRegistry
) : EngineInterface {

    companion object {
        private val API_KEY = BuildConfig.LLM_API_KEY
        private const val MODEL = "openai/gpt-4o-mini"
    }

    private fun getSystemPrompt(): String {
        val appList =
                appRegistry.getAllEntries().joinToString("\n") { entry ->
                    "- ${entry.displayName} (aliases: ${entry.aliases.joinToString(", ")})"
                }

        return """
            You are an Android Intent Extraction Engine.
            Convert the user command into a JSON StructuredIntent.
            
            Schema:
            {
              "intent_type": "OPEN_APP | APP_ACTION | SYSTEM_ACTION | UNKNOWN",
              "target_app": "string | null", // Must match one of the supported apps or their aliases exactly
              "action": "string | null",
              "entities": { "key": "value" },
              "confidence": 0.0
            }
            
            Supported Apps & Aliases:
            $appList
            
            Examples:
            - "Open WhatsApp" -> { "intent_type": "OPEN_APP", "target_app": "WhatsApp", ... }
            - "Increase volume" -> { "intent_type": "APP_ACTION", "target_app": "Volume", "action": "increase", ... }
            - "Turn on flashlight" -> { "intent_type": "APP_ACTION", "target_app": "Flashlight", "action": "turn on", ... }
            - "Mute" -> { "intent_type": "APP_ACTION", "target_app": "Volume", "action": "mute", ... }
            - "Play Bakhuda on Spotify" -> { "intent_type": "APP_ACTION", "target_app": "Spotify", "action": "search", "entities": { "query": "Bakhuda" } }
            - "Search trends on Youtube" -> { "intent_type": "APP_ACTION", "target_app": "YouTube", "action": "search", "entities": { "query": "trends" } }

            Rules:
            - Map commands like "mute", "volume up" explicitly to "Volume" app.
            - Map commands like "torch", "light" explicitly to "Flashlight" app.
            - For "play" or "search" commands with specific content (song, video, artist), use action="search" and map the content to "query" entity.
            - confidence must be between 0.0 and 1.0
            - if intent is unclear or not supported, set intent_type to UNKNOWN
            - STRICT JSON OUTPUT ONLY. NO MARKDOWN. NO EXPLANATION.
        """.trimIndent()
    }

    override suspend fun analyze(command: String): StructuredIntent =
        withContext(Dispatchers.IO) {
            try {
                val messages = listOf(Message(
                    role = "user",
                    content = getSystemPrompt() + "\n\nCommand: " + command
                ))
                val request = ChatCompletionRequest(model = MODEL, messages = messages)

                val response = service.getCompletions("Bearer $API_KEY", request)

                if (!response.isSuccessful || response.body() == null) {
                    Log.e(Constants.LOG.AI_ENGINE, "LLM call failed: ${response.code()}")
                    return@withContext fallbackIntent()
                }

                val chatResponse = response.body()!!
                if (chatResponse.choices.isEmpty()) {
                    return@withContext fallbackIntent()
                }

                val content = chatResponse.choices[0].message.content.trim()
                parseResponseContent(content)
            } catch (e: Exception) {
                Log.e(Constants.LOG.AI_ENGINE, "AI Engine error", e)
                fallbackIntent()
            }
        }

    private fun parseResponseContent(content: String): StructuredIntent {
        return try {
            // Clean up if the model outputs markdown code blocks
            val cleanJson = if (content.startsWith("```json")) {
                 content.removePrefix("```json").removeSuffix("```").trim()
            } else if (content.startsWith("```")) {
                content.removePrefix("```").removeSuffix("```").trim()
            } else {
                content
            }

            val intent = gson.fromJson(cleanJson, StructuredIntent::class.java)

            if (intent.confidence < 0.6) {
                Log.w(Constants.LOG.AI_ENGINE, "Low confidence: ${intent.confidence}")
                fallbackIntent()
            } else {
                intent
            }

        } catch (e: Exception) {
            Log.e(Constants.LOG.AI_ENGINE, "JSON Parsing error", e)
            fallbackIntent()
        }
    }
}
