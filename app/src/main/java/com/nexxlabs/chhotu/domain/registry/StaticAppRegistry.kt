package com.nexxlabs.chhotu.domain.registry

import android.content.Intent
import com.nexxlabs.chhotu.domain.engine.ContactManager
import com.nexxlabs.chhotu.domain.registry.executables.DeepLinkExecutable
import com.nexxlabs.chhotu.domain.registry.executables.IntentExecutable
import com.nexxlabs.chhotu.domain.registry.executables.SystemExecutable
import com.nexxlabs.chhotu.domain.registry.model.Action
import com.nexxlabs.chhotu.domain.registry.model.ActionContract
import com.nexxlabs.chhotu.domain.registry.model.RegistryEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaticAppRegistry @Inject constructor(
    private val contactManager: ContactManager
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
        )
    )

    override fun findByAlias(alias: String): RegistryEntry? {
        val normalizedAlias = alias.lowercase().trim()
        return registry.values.find { it.aliases.contains(normalizedAlias) }
    }
}
