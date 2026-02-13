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
        ),
        // Instagram
        "instagram" to RegistryEntry(
            appId = "instagram",
            displayName = "Instagram",
            packageName = "com.instagram.android",
            aliases = setOf("instagram", "insta"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.instagram.android")
                )
            )
        ),

// Facebook
        "facebook" to RegistryEntry(
            appId = "facebook",
            displayName = "Facebook",
            packageName = "com.facebook.katana",
            aliases = setOf("facebook", "fb"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.facebook.katana")
                )
            )
        ),

// X (Twitter)
        "x" to RegistryEntry(
            appId = "x",
            displayName = "X",
            packageName = "com.twitter.android",
            aliases = setOf("x", "twitter"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.twitter.android")
                )
            )
        ),

// Telegram
        "telegram" to RegistryEntry(
            appId = "telegram",
            displayName = "Telegram",
            packageName = "org.telegram.messenger",
            aliases = setOf("telegram", "tg"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "org.telegram.messenger")
                )
            )
        ),

// Snapchat
        "snapchat" to RegistryEntry(
            appId = "snapchat",
            displayName = "Snapchat",
            packageName = "com.snapchat.android",
            aliases = setOf("snapchat", "snap"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.snapchat.android")
                )
            )
        ),

// Threads
        "threads" to RegistryEntry(
            appId = "threads",
            displayName = "Threads",
            packageName = "com.instagram.barcelona",
            aliases = setOf("threads"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.instagram.barcelona")
                )
            )
        ),

// Google Chrome
        "chrome" to RegistryEntry(
            appId = "chrome",
            displayName = "Google Chrome",
            packageName = "com.android.chrome",
            aliases = setOf("chrome", "browser"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.android.chrome")
                ),
                Action(
                    id = "SEARCH",
                    aliases = setOf("search", "find"),
                    contract = ActionContract(setOf("query")),
                    primaryExecutable = DeepLinkExecutable("https://www.google.com/search?q={query}")
                )
            )
        ),

// Gmail
        "gmail" to RegistryEntry(
            appId = "gmail",
            displayName = "Gmail",
            packageName = "com.google.android.gm",
            aliases = setOf("gmail", "email"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.gm")
                )
            )
        ),

// Google Maps
        "maps" to RegistryEntry(
            appId = "maps",
            displayName = "Google Maps",
            packageName = "com.google.android.apps.maps",
            aliases = setOf("maps", "google maps", "navigation"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.apps.maps")
                ),
                Action(
                    id = "SEARCH",
                    aliases = setOf("search", "navigate"),
                    contract = ActionContract(setOf("query")),
                    primaryExecutable = DeepLinkExecutable("geo:0,0?q={query}")
                )
            )
        ),

// Spotify
        "spotify" to RegistryEntry(
            appId = "spotify",
            displayName = "Spotify",
            packageName = "com.spotify.music",
            aliases = setOf("spotify", "music"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "play"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.spotify.music")
                )
            )
        ),

// Netflix
        "netflix" to RegistryEntry(
            appId = "netflix",
            displayName = "Netflix",
            packageName = "com.netflix.mediaclient",
            aliases = setOf("netflix"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.netflix.mediaclient")
                )
            )
        ),

// Zomato
        "zomato" to RegistryEntry(
            appId = "zomato",
            displayName = "Zomato",
            packageName = "com.application.zomato",
            aliases = setOf("zomato", "food"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "order food"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.application.zomato")
                )
            )
        ),

// Swiggy
        "swiggy" to RegistryEntry(
            appId = "swiggy",
            displayName = "Swiggy",
            packageName = "in.swiggy.android",
            aliases = setOf("swiggy"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "order food"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "in.swiggy.android")
                )
            )
        ),

// Google Pay
        "gpay" to RegistryEntry(
            appId = "gpay",
            displayName = "Google Pay",
            packageName = "com.google.android.apps.nbu.paisa.user",
            aliases = setOf("gpay", "google pay"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "pay"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.apps.nbu.paisa.user")
                )
            )
        ),

// Uber
        "uber" to RegistryEntry(
            appId = "uber",
            displayName = "Uber",
            packageName = "com.ubercab",
            aliases = setOf("uber", "cab"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "book cab"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.ubercab")
                )
            )
        ),

// ChatGPT
        "chatgpt" to RegistryEntry(
            appId = "chatgpt",
            displayName = "ChatGPT",
            packageName = "com.openai.chatgpt",
            aliases = setOf("chatgpt", "gpt"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.openai.chatgpt")
                )
            )
        ),
        // Amazon Prime Video
        "prime_video" to RegistryEntry(
            appId = "prime_video",
            displayName = "Amazon Prime Video",
            packageName = "com.amazon.avod.thirdpartyclient",
            aliases = setOf("prime video", "amazon prime", "prime"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.amazon.avod.thirdpartyclient")
                )
            )
        ),

// Disney+ Hotstar
        "hotstar" to RegistryEntry(
            appId = "hotstar",
            displayName = "Disney+ Hotstar",
            packageName = "in.startv.hotstar",
            aliases = setOf("hotstar", "disney hotstar"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "in.startv.hotstar")
                )
            )
        ),

// JioCinema
        "jiocinema" to RegistryEntry(
            appId = "jiocinema",
            displayName = "JioCinema",
            packageName = "com.jio.media.ondemand",
            aliases = setOf("jiocinema", "jio cinema"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.jio.media.ondemand")
                )
            )
        ),

// Amazon
        "amazon" to RegistryEntry(
            appId = "amazon",
            displayName = "Amazon",
            packageName = "in.amazon.mShop.android.shopping",
            aliases = setOf("amazon", "shopping"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "shop"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "in.amazon.mShop.android.shopping")
                )
            )
        ),

// Flipkart
        "flipkart" to RegistryEntry(
            appId = "flipkart",
            displayName = "Flipkart",
            packageName = "com.flipkart.android",
            aliases = setOf("flipkart"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "shop"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.flipkart.android")
                )
            )
        ),

// Myntra
        "myntra" to RegistryEntry(
            appId = "myntra",
            displayName = "Myntra",
            packageName = "com.myntra.android",
            aliases = setOf("myntra"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "shop"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.myntra.android")
                )
            )
        ),

// Blinkit
        "blinkit" to RegistryEntry(
            appId = "blinkit",
            displayName = "Blinkit",
            packageName = "com.grofers.customerapp",
            aliases = setOf("blinkit", "groceries"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "order groceries"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.grofers.customerapp")
                )
            )
        ),

// Zepto
        "zepto" to RegistryEntry(
            appId = "zepto",
            displayName = "Zepto",
            packageName = "com.zeptoconsumerapp",
            aliases = setOf("zepto"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "order groceries"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.zeptoconsumerapp")
                )
            )
        ),

// BigBasket
        "bigbasket" to RegistryEntry(
            appId = "bigbasket",
            displayName = "BigBasket",
            packageName = "com.bigbasket.mobileapp",
            aliases = setOf("bigbasket"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "order groceries"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.bigbasket.mobileapp")
                )
            )
        ),

// PhonePe
        "phonepe" to RegistryEntry(
            appId = "phonepe",
            displayName = "PhonePe",
            packageName = "com.phonepe.app",
            aliases = setOf("phonepe"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "pay"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.phonepe.app")
                )
            )
        ),

// Paytm
        "paytm" to RegistryEntry(
            appId = "paytm",
            displayName = "Paytm",
            packageName = "net.one97.paytm",
            aliases = setOf("paytm"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "pay"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "net.one97.paytm")
                )
            )
        ),

// Ola
        "ola" to RegistryEntry(
            appId = "ola",
            displayName = "Ola",
            packageName = "com.olacabs.customer",
            aliases = setOf("ola"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "book cab"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.olacabs.customer")
                )
            )
        ),

// Truecaller
        "truecaller" to RegistryEntry(
            appId = "truecaller",
            displayName = "Truecaller",
            packageName = "com.truecaller",
            aliases = setOf("truecaller", "caller id"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.truecaller")
                )
            )
        ),

// Zoom
        "zoom" to RegistryEntry(
            appId = "zoom",
            displayName = "Zoom",
            packageName = "us.zoom.videomeetings",
            aliases = setOf("zoom", "meeting"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "join meeting"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "us.zoom.videomeetings")
                )
            )
        ),

// Canva
        "canva" to RegistryEntry(
            appId = "canva",
            displayName = "Canva",
            packageName = "com.canva.editor",
            aliases = setOf("canva", "design"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "design"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.canva.editor")
                )
            )
        ),
        // Google Photos
        "photos" to RegistryEntry(
            appId = "photos",
            displayName = "Google Photos",
            packageName = "com.google.android.apps.photos",
            aliases = setOf("photos", "gallery", "google photos"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.apps.photos")
                )
            )
        ),

// Google Drive
        "drive" to RegistryEntry(
            appId = "drive",
            displayName = "Google Drive",
            packageName = "com.google.android.apps.docs",
            aliases = setOf("drive", "google drive"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "launch"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.apps.docs")
                )
            )
        ),

// Google Translate
        "translate" to RegistryEntry(
            appId = "translate",
            displayName = "Google Translate",
            packageName = "com.google.android.apps.translate",
            aliases = setOf("translate", "translator"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "translate"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.google.android.apps.translate")
                )
            )
        ),

// Meesho
        "meesho" to RegistryEntry(
            appId = "meesho",
            displayName = "Meesho",
            packageName = "com.meesho.supply",
            aliases = setOf("meesho"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "shop"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.meesho.supply")
                )
            )
        ),

// Ajio
        "ajio" to RegistryEntry(
            appId = "ajio",
            displayName = "Ajio",
            packageName = "com.ril.ajio",
            aliases = setOf("ajio"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "shop"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.ril.ajio")
                )
            )
        ),

// Swiggy Instamart
        "instamart" to RegistryEntry(
            appId = "instamart",
            displayName = "Swiggy Instamart",
            packageName = "in.swiggy.android",
            aliases = setOf("instamart", "swiggy instamart"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "order groceries"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "in.swiggy.android")
                )
            )
        ),

// BHIM
        "bhim" to RegistryEntry(
            appId = "bhim",
            displayName = "BHIM",
            packageName = "in.org.npci.upiapp",
            aliases = setOf("bhim", "upi"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "pay"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "in.org.npci.upiapp")
                )
            )
        ),

// Rapido
        "rapido" to RegistryEntry(
            appId = "rapido",
            displayName = "Rapido",
            packageName = "com.rapido.passenger",
            aliases = setOf("rapido", "bike taxi"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "book bike"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.rapido.passenger")
                )
            )
        ),

// IRCTC Rail Connect
        "irctc" to RegistryEntry(
            appId = "irctc",
            displayName = "IRCTC Rail Connect",
            packageName = "cris.org.in.prs.ima",
            aliases = setOf("irctc", "train booking"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "book train"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "cris.org.in.prs.ima")
                )
            )
        ),

// MakeMyTrip
        "makemytrip" to RegistryEntry(
            appId = "makemytrip",
            displayName = "MakeMyTrip",
            packageName = "com.makemytrip",
            aliases = setOf("makemytrip", "mmt"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "book travel"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.makemytrip")
                )
            )
        ),

// MX Player
        "mxplayer" to RegistryEntry(
            appId = "mxplayer",
            displayName = "MX Player",
            packageName = "com.mxtech.videoplayer.ad",
            aliases = setOf("mx player", "mx"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "play video"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.mxtech.videoplayer.ad")
                )
            )
        ),

// ShareChat
        "sharechat" to RegistryEntry(
            appId = "sharechat",
            displayName = "ShareChat",
            packageName = "in.mohalla.sharechat",
            aliases = setOf("sharechat"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "in.mohalla.sharechat")
                )
            )
        ),

// Josh
        "josh" to RegistryEntry(
            appId = "josh",
            displayName = "Josh",
            packageName = "com.eterno.shortvideos",
            aliases = setOf("josh"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.eterno.shortvideos")
                )
            )
        ),

// Dailyhunt
        "dailyhunt" to RegistryEntry(
            appId = "dailyhunt",
            displayName = "Dailyhunt",
            packageName = "com.eterno",
            aliases = setOf("dailyhunt", "news"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "read news"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.eterno")
                )
            )
        ),

// Inshorts
        "inshorts" to RegistryEntry(
            appId = "inshorts",
            displayName = "Inshorts",
            packageName = "com.nis.app",
            aliases = setOf("inshorts", "short news"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "read news"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.nis.app")
                )
            )
        ),

// Microsoft Teams
        "teams" to RegistryEntry(
            appId = "teams",
            displayName = "Microsoft Teams",
            packageName = "com.microsoft.teams",
            aliases = setOf("teams", "microsoft teams"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "join meeting"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.microsoft.teams")
                )
            )
        ),

// CapCut
        "capcut" to RegistryEntry(
            appId = "capcut",
            displayName = "CapCut",
            packageName = "com.lemon.lvoverseas",
            aliases = setOf("capcut", "video editor"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    aliases = setOf("open", "edit video"),
                    contract = ActionContract(emptySet()),
                    primaryExecutable = IntentExecutable(Intent.ACTION_MAIN, "com.lemon.lvoverseas")
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
