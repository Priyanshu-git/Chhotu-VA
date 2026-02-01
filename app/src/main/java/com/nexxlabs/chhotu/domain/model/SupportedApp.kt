package com.nexxlabs.chhotu.domain.model

/**
 * Represents the supported apps that Chhotu can interact with.
 * Each app has a package name for package visibility and intent execution,
 * a display name for TTS feedback, and keywords for command matching.
 */
enum class SupportedApp(
    val packageName: String,
    val displayName: String,
    val keywords: List<String>
) {
    GOOGLE_SEARCH(
        packageName = "com.google.android.googlequicksearchbox",
        displayName = "Google",
        keywords = listOf("google", "search")
    ),
    YOUTUBE_MUSIC(
        packageName = "com.google.android.apps.youtube.music",
        displayName = "YouTube Music",
        keywords = listOf("youtube music", "music")
    ),
    WHATSAPP(
        packageName = "com.whatsapp",
        displayName = "WhatsApp",
        keywords = listOf("whatsapp", "whats app")
    );

    companion object {
        /**
         * Find a supported app by matching keywords in the input text.
         * Returns null if no app matches.
         */
        fun fromKeyword(input: String): SupportedApp? {
            val lowercaseInput = input.lowercase()
            // Check YouTube Music first (more specific match)
            if (YOUTUBE_MUSIC.keywords.any { lowercaseInput.contains(it) }) {
                return YOUTUBE_MUSIC
            }
            return entries.firstOrNull { app ->
                app.keywords.any { keyword -> lowercaseInput.contains(keyword) }
            }
        }
    }
}
