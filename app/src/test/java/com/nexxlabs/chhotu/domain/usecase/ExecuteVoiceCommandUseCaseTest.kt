package com.nexxlabs.chhotu.domain.usecase

import android.util.Log
import com.nexxlabs.chhotu.domain.engine.CapabilityResolver
import com.nexxlabs.chhotu.domain.engine.CommandNormalizer
import com.nexxlabs.chhotu.domain.engine.EngineInterface
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExecuteVoiceCommandUseCaseTest {

    private lateinit var normalizer: CommandNormalizer
    private lateinit var basicEngine: EngineInterface
    private lateinit var aiEngine: EngineInterface
    private lateinit var resolver: CapabilityResolver
    private lateinit var useCase: ExecuteVoiceCommandUseCase

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0

        normalizer = CommandNormalizer()
        basicEngine = mockk()
        aiEngine = mockk()
        resolver = mockk()

        useCase = ExecuteVoiceCommandUseCase(normalizer, basicEngine, aiEngine, resolver)
    }

    @Test
    fun `uses basic engine result when not UNKNOWN`() = runBlocking {
        val intent = StructuredIntent(IntentType.OPEN_APP, "whatsapp", null, emptyMap(), 1.0)
        val expected = CommandResult(ExecutionResult.Success, displayName = "WhatsApp")

        coEvery { basicEngine.analyze("open whatsapp") } returns intent
        every { resolver.resolveAndExecute(intent) } returns expected

        val result = useCase.execute("Open WhatsApp")

        assertEquals(expected, result)
    }

    @Test
    fun `falls back to AI engine when basic returns UNKNOWN`() = runBlocking {
        val unknownIntent = StructuredIntent(IntentType.UNKNOWN, null, null, emptyMap(), 0.0)
        val aiIntent = StructuredIntent(IntentType.APP_ACTION, "Volume", "mute", emptyMap(), 0.95)
        val expected = CommandResult(ExecutionResult.Success, displayName = "Volume Control")

        coEvery { basicEngine.analyze("mute") } returns unknownIntent
        coEvery { aiEngine.analyze("mute") } returns aiIntent
        every { resolver.resolveAndExecute(aiIntent) } returns expected

        val result = useCase.execute("Mute")

        assertEquals(expected, result)
    }

    @Test
    fun `executeIntent delegates directly to resolver`() {
        val intent = StructuredIntent(IntentType.OPEN_APP, "whatsapp", null, emptyMap(), 1.0)
        val expected = CommandResult(ExecutionResult.Success)

        every { resolver.resolveAndExecute(intent) } returns expected

        val result = useCase.executeIntent(intent)

        assertEquals(expected, result)
        verify { resolver.resolveAndExecute(intent) }
    }
}
