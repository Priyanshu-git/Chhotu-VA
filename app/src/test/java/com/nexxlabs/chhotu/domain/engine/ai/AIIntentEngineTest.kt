package com.nexxlabs.chhotu.domain.engine.ai

import android.util.Log
import com.google.gson.Gson
import com.nexxlabs.chhotu.data.remote.OpenRouterService
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.domain.registry.model.RegistryEntry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AIIntentEngineTest {

    private lateinit var client: OpenRouterService
    private lateinit var gson: Gson
    private lateinit var appRegistry: AppRegistry
    private lateinit var aiIntentEngine: AIIntentEngine

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        client = mockk()
        gson = Gson()
        appRegistry = mockk()

        // Mock app registry for prompt generation
        every { appRegistry.getAllEntries() } returns
                listOf(
                    RegistryEntry(
                        "whatsapp",
                        "WhatsApp",
                        "com.whatsapp",
                        setOf("whatsapp"),
                        emptySet()
                    ),
                    RegistryEntry("volume", "Volume", null, setOf("volume"), emptySet())
                )

        aiIntentEngine = AIIntentEngine(client, gson, appRegistry)
    }

    @Test
    fun `analyze returns correct intent on successful API call`() = runBlocking {
        val command = "mute"
        val mockJsonResponse =
            """
            {
              "id": "123",
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": "{\"intent_type\": \"APP_ACTION\", \"target_app\": \"Volume\", \"action\": \"mute\", \"entities\": {}, \"confidence\": 0.95}"
                  }
                }
              ]
            }
        """.trimIndent()

        val responseObj =
            gson.fromJson(
                mockJsonResponse,
                com.nexxlabs.chhotu.data.remote.model.ChatCompletionResponse::class.java
            )

        coEvery { client.getCompletions(any(), any()) } returns Response.success(responseObj)

        val result = aiIntentEngine.analyze(command)

        assertEquals(IntentType.APP_ACTION, result.intentType)
        assertEquals("Volume", result.targetApp)
        assertEquals("mute", result.action)
        assertEquals(0.95, result.confidence, 0.01)
    }

    @Test
    fun `analyze returns fallback intent on API failure`() = runBlocking {
        val command = "test"

        coEvery { client.getCompletions(any(), any()) } returns
                Response.error(500, "Error".toResponseBody())

        val result = aiIntentEngine.analyze(command)

        assertEquals(IntentType.UNKNOWN, result.intentType)
    }

    @Test
    fun `analyze handles markdown code blocks in response`() = runBlocking {
        val command = "open whatsapp"
        val mockJsonResponse =
            """
            {
              "id": "124",
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": "```json\n{\"intent_type\": \"OPEN_APP\", \"target_app\": \"whatsapp\", \"action\": \"OPEN\", \"entities\": {}, \"confidence\": 1.0}\n```"
                  }
                }
              ]
            }
        """.trimIndent()

        val responseObj =
            gson.fromJson(
                mockJsonResponse,
                com.nexxlabs.chhotu.data.remote.model.ChatCompletionResponse::class.java
            )

        coEvery { client.getCompletions(any(), any()) } returns Response.success(responseObj)

        val result = aiIntentEngine.analyze(command)

        assertEquals(IntentType.OPEN_APP, result.intentType)
        assertEquals("whatsapp", result.targetApp)
    }
}
