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
        assertTrue(entries.any { it.appId == "sms" })
        assertTrue(entries.any { it.appId == "google" })
        assertTrue(entries.any { it.appId == "settings" })
        assertTrue(entries.any { it.appId == "volume" })
        assertTrue(entries.any { it.appId == "flashlight" })
        assertTrue(entries.any { it.appId == "clock" })
        assertTrue(entries.any { it.appId == "camera" })
        assertTrue(entries.any { it.appId == "instagram" })
        assertTrue(entries.any { it.appId == "facebook" })
        assertTrue(entries.any { it.appId == "x" })
        assertTrue(entries.any { it.appId == "telegram" })
        assertTrue(entries.any { it.appId == "snapchat" })
        assertTrue(entries.any { it.appId == "threads" })
        assertTrue(entries.any { it.appId == "chrome" })
        assertTrue(entries.any { it.appId == "gmail" })
        assertTrue(entries.any { it.appId == "maps" })
        assertTrue(entries.any { it.appId == "spotify" })
        assertTrue(entries.any { it.appId == "netflix" })
        assertTrue(entries.any { it.appId == "zomato" })
        assertTrue(entries.any { it.appId == "swiggy" })
        assertTrue(entries.any { it.appId == "gpay" })
        assertTrue(entries.any { it.appId == "uber" })
        assertTrue(entries.any { it.appId == "chatgpt" })
        assertTrue(entries.any { it.appId == "prime_video" })
        assertTrue(entries.any { it.appId == "hotstar" })
        assertTrue(entries.any { it.appId == "jiocinema" })
        assertTrue(entries.any { it.appId == "amazon" })
        assertTrue(entries.any { it.appId == "flipkart" })
        assertTrue(entries.any { it.appId == "myntra" })
        assertTrue(entries.any { it.appId == "blinkit" })
        assertTrue(entries.any { it.appId == "zepto" })
        assertTrue(entries.any { it.appId == "bigbasket" })
        assertTrue(entries.any { it.appId == "phonepe" })
        assertTrue(entries.any { it.appId == "paytm" })
        assertTrue(entries.any { it.appId == "ola" })
        assertTrue(entries.any { it.appId == "truecaller" })
        assertTrue(entries.any { it.appId == "zoom" })
        assertTrue(entries.any { it.appId == "canva" })
        assertTrue(entries.any { it.appId == "photos" })
        assertTrue(entries.any { it.appId == "drive" })
        assertTrue(entries.any { it.appId == "translate" })
        assertTrue(entries.any { it.appId == "meesho" })
        assertTrue(entries.any { it.appId == "ajio" })
        assertTrue(entries.any { it.appId == "instamart" })
        assertTrue(entries.any { it.appId == "bhim" })
        assertTrue(entries.any { it.appId == "rapido" })
        assertTrue(entries.any { it.appId == "irctc" })
        assertTrue(entries.any { it.appId == "makemytrip" })
        assertTrue(entries.any { it.appId == "mxplayer" })
        assertTrue(entries.any { it.appId == "sharechat" })
        assertTrue(entries.any { it.appId == "josh" })
        assertTrue(entries.any { it.appId == "dailyhunt" })
        assertTrue(entries.any { it.appId == "inshorts" })
        assertTrue(entries.any { it.appId == "teams" })
        assertTrue(entries.any { it.appId == "capcut" })

        // Check total count
        assertEquals(58, entries.size)
    }

    @Test
    fun `Volume registry has INCREASE action with alias`() {
        val entry = staticAppRegistry.findByAlias("volume")
        val increaseAction = entry?.actions?.find { it.id == "INCREASE" }

        assertNotNull(increaseAction)
        assertTrue(increaseAction?.aliases?.contains("louder") == true)
    }
}
