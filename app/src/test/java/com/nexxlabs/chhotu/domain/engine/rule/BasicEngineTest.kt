package com.nexxlabs.chhotu.domain.engine.rule

import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BasicEngineTest {

    private val engine = BasicEngine()

    @Test
    fun `open command returns OPEN_APP intent`() = runBlocking {
        val result = engine.analyze("open whatsapp")
        assertEquals(IntentType.OPEN_APP, result.intentType)
        assertEquals("whatsapp", result.targetApp)
        assertEquals(1.0, result.confidence, 0.01)
    }

    @Test
    fun `launch command returns OPEN_APP intent`() = runBlocking {
        val result = engine.analyze("launch spotify")
        assertEquals(IntentType.OPEN_APP, result.intentType)
        assertEquals("spotify", result.targetApp)
    }

    @Test
    fun `start command returns OPEN_APP intent`() = runBlocking {
        val result = engine.analyze("start chrome")
        assertEquals(IntentType.OPEN_APP, result.intentType)
        assertEquals("chrome", result.targetApp)
    }

    @Test
    fun `call command returns SYSTEM_ACTION with contact entity`() = runBlocking {
        val result = engine.analyze("call john")
        assertEquals(IntentType.SYSTEM_ACTION, result.intentType)
        assertEquals("Phone", result.targetApp)
        assertEquals("call", result.action)
        assertEquals("john", result.entities["contact"])
    }

    @Test
    fun `call with multi-word name captures full name`() = runBlocking {
        val result = engine.analyze("call john doe")
        assertEquals("john doe", result.entities["contact"])
    }

    @Test
    fun `single word returns UNKNOWN fallback`() = runBlocking {
        val result = engine.analyze("hello")
        assertEquals(IntentType.UNKNOWN, result.intentType)
    }

    @Test
    fun `unknown command returns UNKNOWN fallback`() = runBlocking {
        val result = engine.analyze("play music loudly")
        assertEquals(IntentType.UNKNOWN, result.intentType)
    }

    @Test
    fun `open multi-word app name captures full name`() = runBlocking {
        val result = engine.analyze("open google maps")
        assertEquals(IntentType.OPEN_APP, result.intentType)
        assertEquals("google maps", result.targetApp)
    }
}
