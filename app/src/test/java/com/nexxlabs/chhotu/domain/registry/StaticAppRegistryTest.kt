package com.nexxlabs.chhotu.domain.registry

import android.content.Context
import com.nexxlabs.chhotu.domain.engine.ContactManager
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StaticAppRegistryTest {

    private lateinit var context: Context
    private lateinit var contactManager: ContactManager
    private lateinit var staticAppRegistry: StaticAppRegistry

    @Before
    fun setup() {
        context = mockk()
        contactManager = mockk()
        staticAppRegistry = StaticAppRegistry(context, contactManager)
    }

    @Test
    fun `findByAlias finds WhatsApp by its primary name`() {
        val entry = staticAppRegistry.findByAlias("whatsapp")
        assertNotNull(entry)
        assertEquals("whatsapp", entry?.appId)
    }

    @Test
    fun `findByAlias finds WhatsApp by its alias`() {
        val entry = staticAppRegistry.findByAlias("msg")
        assertNotNull(entry)
        assertEquals("whatsapp", entry?.appId)
    }

    @Test
    fun `findByAlias finds Volume Control by alias`() {
        val entry = staticAppRegistry.findByAlias("sound")
        assertNotNull(entry)
        assertEquals("volume", entry?.appId)
    }

    @Test
    fun `getAllEntries returns all registered apps`() {
        val entries = staticAppRegistry.getAllEntries()

        // Ensure core apps are present
        assertTrue(entries.any { it.appId == "whatsapp" })
        assertTrue(entries.any { it.appId == "youtube" })
        assertTrue(entries.any { it.appId == "phone" })
        assertTrue(entries.any { it.appId == "volume" })
        assertTrue(entries.any { it.appId == "flashlight" })

        // Check total count (based on current implementation there are 10: whatsapp, youtube,
        // phone, sms, google, settings, volume, flashlight, clock, camera)
        assertEquals(10, entries.size)
    }

    @Test
    fun `Volume registry has INCREASE action with alias`() {
        val entry = staticAppRegistry.findByAlias("volume")
        val increaseAction = entry?.actions?.find { it.id == "INCREASE" }

        assertNotNull(increaseAction)
        assertTrue(increaseAction?.aliases?.contains("louder") == true)
    }
}
