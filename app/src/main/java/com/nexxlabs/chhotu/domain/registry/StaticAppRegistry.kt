package com.nexxlabs.chhotu.domain.registry

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.nexxlabs.chhotu.domain.engine.ContactManager
import com.nexxlabs.chhotu.domain.registry.executables.DeepLinkExecutable
import com.nexxlabs.chhotu.domain.registry.executables.FlashlightExecutable
import com.nexxlabs.chhotu.domain.registry.executables.IntentExecutable
import com.nexxlabs.chhotu.domain.registry.executables.SystemExecutable
import com.nexxlabs.chhotu.domain.registry.executables.VolumeExecutable
import com.nexxlabs.chhotu.domain.registry.model.Action
import com.nexxlabs.chhotu.domain.registry.model.ActionContract
import com.nexxlabs.chhotu.domain.registry.model.RegistryEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaticAppRegistry @Inject constructor(
    @ApplicationContext private val context: Context, private val contactManager: ContactManager
) : AppRegistry {

    private val registry = mapOf(
        // WhatsApp
        "whatsapp" to RegistryEntry(
            appId = "whatsapp",
            displayName = "WhatsApp",
            packageName = "com.whatsapp",
            aliases = setOf("whatsapp", "whats app", "msg"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch", "start"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.whatsapp")
                ),
                Action(
                    id = "SEND_MESSAGE",
                    aliases = setOf("send message", "text", "message", "send"),
                    contract = ActionContract(setOf("contact", "text")),
                    primaryExecutable = IntentExecutable(Intent.ACTION_SEND, "com.whatsapp"), // Simplified, typically needs specialized intent for specific contact
                    fallbackExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.whatsapp")
                )
            )
        ),
        
        // YouTube
        "youtube" to RegistryEntry(
            appId = "youtube",
            displayName = "YouTube",
            packageName = "com.google.android.youtube",
            aliases = setOf("youtube", "yt"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch", "start"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.youtube")
                ),
                Action(
                    id = "SEARCH",
                    aliases = setOf("search", "find", "look for", "play"),
                    contract = ActionContract(setOf("query")),
                    primaryExecutable = DeepLinkExecutable("https://www.youtube.com/results?search_query={query}"),
                    fallbackExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.youtube")
                )
            )
        ),

        // Phone
        "phone" to RegistryEntry(
            appId = "phone",
            displayName = "Phone",
            packageName = "com.google.android.dialer", // Targeted but SystemExecutable handles intent
            aliases = setOf("phone", "call", "dialer"),
            actions = setOf(
                Action(
                    id = "CALL",
                    aliases = setOf("call", "dial", "ring"),
                    contract = ActionContract(setOf("contact")),
                    primaryExecutable = SystemExecutable(Intent.ACTION_CALL, "tel", contactManager),
                    fallbackExecutable = SystemExecutable(Intent.ACTION_DIAL, "tel", contactManager) 
                )
            )
        ),

        // SMS
        "sms" to RegistryEntry(
            appId = "sms",
            displayName = "SMS",
            packageName = "com.google.android.apps.messaging",
            aliases = setOf("sms", "text", "message"),
            actions = setOf(
                Action(
                    id = "SEND_MESSAGE",
                    aliases = setOf("send message", "text", "send sms", "message"),
                    contract = ActionContract(setOf("contact", "text")),
                    primaryExecutable = SystemExecutable(Intent.ACTION_SENDTO, "smsto", contactManager), // Uses SystemExecutable for Uri based intent
                    fallbackExecutable = null
                )
            )
        ),

        // Google Search
        "google" to RegistryEntry(
            appId = "google",
            displayName = "Google",
            packageName = "com.google.android.googlequicksearchbox",
            aliases = setOf("google", "search", "google search"),
            actions = setOf(
                Action(
                    id = "SEARCH",
                    aliases = setOf("search", "google", "find", "look up"),
                    contract = ActionContract(setOf("query")),
                    primaryExecutable = IntentExecutable(Intent.ACTION_WEB_SEARCH, "com.google.android.googlequicksearchbox"),
                    fallbackExecutable = DeepLinkExecutable("https://www.google.com/search?q={query}")
                )
            )
        ),

        // Settings (New)
        "settings" to RegistryEntry(
            appId = "settings",
            displayName = "Settings",
            packageName = "com.android.settings",
            aliases = setOf("settings", "configuration", "preferences"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch", "show"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Settings.ACTION_SETTINGS, "com.android.settings")
                ),
                Action(
                    id = "WIFI_SETTINGS",
                    aliases = setOf("wifi", "network", "internet"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Settings.ACTION_WIFI_SETTINGS, "com.android.settings")
                ),
                Action(
                    id = "BLUETOOTH_SETTINGS",
                    aliases = setOf("bluetooth", "bt"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Settings.ACTION_BLUETOOTH_SETTINGS, "com.android.settings")
                )
            )
        ),

        // Volume (New)
        "volume" to RegistryEntry(
            appId = "volume",
            displayName = "Volume Control",
            packageName = null, // System service
            aliases = setOf("volume", "sound", "audio"),
            actions = setOf(
                Action(
                    id = "INCREASE",
                    aliases = setOf("increase", "turn up", "raise", "louder"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = VolumeExecutable(VolumeExecutable.VolumeAction.INCREASE)
                ),
                Action(
                    id = "DECREASE",
                    aliases = setOf("decrease", "turn down", "lower", "softer", "quieter"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = VolumeExecutable(VolumeExecutable.VolumeAction.DECREASE)
                ),
                Action(
                    id = "MUTE",
                    aliases = setOf("mute", "silence", "silent", "shut up"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = VolumeExecutable(VolumeExecutable.VolumeAction.MUTE)
                )
            )
        ),

        // Flashlight (New)
        "flashlight" to RegistryEntry(
            appId = "flashlight",
            displayName = "Flashlight",
            packageName = null,
            aliases = setOf("flashlight", "torch", "light"),
            actions = setOf(
                Action(
                    id = "TURN_ON",
                    aliases = setOf("on", "enable", "start", "open"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = FlashlightExecutable(FlashlightExecutable.FlashlightAction.ON)
                ),
                Action(
                    id = "TURN_OFF",
                    aliases = setOf("off", "disable", "stop", "close"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = FlashlightExecutable(FlashlightExecutable.FlashlightAction.OFF)
                )
            )
        ),

        // Clock (New)
        "clock" to RegistryEntry(
            appId = "clock",
            displayName = "Clock",
            packageName = "com.google.android.deskclock", // Standard Google
            aliases = setOf("clock", "alarm", "timer", "stopwatch"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch", "show"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(AlarmClock.ACTION_SHOW_ALARMS, "com.google.android.deskclock"),
                    fallbackExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.deskclock")
                )
            )
        ),

        // Camera (New)
        "camera" to RegistryEntry(
            appId = "camera",
            displayName = "Camera",
            packageName = "com.android.camera2", // Varies heavily. Better to use
            // strict Action fallback
            aliases = setOf("camera", "photo", "picture"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch", "take photo"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA, "com.google.android.GoogleCamera"), // Pixel
                    fallbackExecutable = SystemExecutable(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA) // Generic Intent
                )
            )
        )
    )

    override fun findByAlias(alias: String): RegistryEntry? {
        val normalizedAlias = alias.lowercase().trim()
        return registry.values.find { it.aliases.contains(normalizedAlias) }
    }

    override fun getAllEntries(): List<RegistryEntry> {
        return registry.values.toList()
    }
}
