package com.nexxlabs.chhotu.domain.engine.ai

import android.util.Log
import com.google.gson.Gson
import com.nexxlabs.chhotu.BuildConfig
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIIntentEngine @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson
) {

    companion object {
        private const val OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"
        private val API_KEY = BuildConfig.OPEN_ROUTER_API_KEY
        private const val MODEL = "google/gemma-3-4b-it:free"
        private const val SYSTEM_PROMPT = """
            You are an Android Intent Extraction Engine.
            Convert the user command into a JSON StructuredIntent.
            
            Schema:
            {
              "intent_type": "OPEN_APP | APP_ACTION | SYSTEM_ACTION | UNKNOWN",
              "target_app": "string | null",
              "action": "string | null",
              "entities": { "key": "value" },
              "confidence": 0.0
            }
            
            supported_apps: [WhatsApp, YouTube, Phone, SMS, Google Search]
            
            Rules:
            - confidence must be between 0.0 and 1.0
            - if intent is unclear or not supported, set intent_type to UNKNOWN
            - STRICT JSON OUTPUT ONLY. NO MARKDOWN. NO EXPLANATION.
        """
    }

    suspend fun analyze(command: String): StructuredIntent = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("model", MODEL)
                put("messages", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", SYSTEM_PROMPT + "\n\nCommand: " + command)
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(OPENROUTER_API_URL)
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(Constants.LOG.AI_ENGINE, "OpenRouter call failed: ${response.code}")
                return@withContext fallbackIntent()
            }

            parseResponse(responseBody)

        } catch (e: Exception) {
            Log.e(Constants.LOG.AI_ENGINE, "AI Engine error", e)
            fallbackIntent()
        }
    }

    private fun parseResponse(jsonResponse: String): StructuredIntent {
        return try {
            val root = JSONObject(jsonResponse)
            val content = root.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
                
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

    private fun fallbackIntent(): StructuredIntent {
        return StructuredIntent(
            intentType = IntentType.UNKNOWN,
            targetApp = null,
            action = null,
            entities = emptyMap(),
            confidence = 0.0
        )
    }
}
