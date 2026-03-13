package com.nexxlabs.chhotu.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandNormalizerTest {

    private val normalizer = CommandNormalizer()

    @Test
    fun `normalize converts to lowercase`() {
        assertEquals("open whatsapp", normalizer.normalize("Open WhatsApp"))
    }

    @Test
    fun `normalize trims whitespace`() {
        assertEquals("call john", normalizer.normalize("  call john  "))
    }

    @Test
    fun `normalize collapses extra whitespace`() {
        assertEquals("open google maps", normalizer.normalize("open  google   maps"))
    }

    @Test
    fun `normalize handles empty string`() {
        assertEquals("", normalizer.normalize(""))
    }

    @Test
    fun `normalize handles whitespace-only string`() {
        assertEquals("", normalizer.normalize("   "))
    }
}
