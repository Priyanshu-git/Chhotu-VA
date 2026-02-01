package com.nexxlabs.chhotu.domain.model

/**
 * Sealed class representing the different types of commands the assistant can handle.
 * Each subclass contains the relevant parameters extracted from user input.
 * 
 * This architecture allows for easy extension - new command types can be added
 * as additional subclasses without modifying existing code.
 */
sealed class CommandIntent {
    
    /**
     * Search query on Google.
     * @param query The search query to execute
     */
    data class Search(val query: String) : CommandIntent() {
        val feedbackMessage: String
            get() = "Searching $query on Google"
    }
    
    /**
     * Play music on YouTube Music.
     * @param query The artist, song, or album to search for
     */
    data class PlayMusic(val query: String) : CommandIntent() {
        val feedbackMessage: String
            get() = "Playing $query on YouTube Music"
    }
    
    /**
     * Send a message via WhatsApp.
     * @param message The message content to send
     * @param recipient Optional recipient name (for future use)
     */
    data class SendWhatsAppMessage(
        val message: String,
        val recipient: String? = null
    ) : CommandIntent() {
        val feedbackMessage: String
            get() = "Opening WhatsApp to send your message"
    }
    
    /**
     * Open a specific app.
     * @param app The target app to open
     */
    data class OpenApp(val app: SupportedApp) : CommandIntent() {
        val feedbackMessage: String
            get() = "Opening ${app.displayName}"
    }
    
    /**
     * Unknown command - could not be parsed.
     * @param originalText The original user input that couldn't be understood
     */
    data class Unknown(val originalText: String) : CommandIntent() {
        val feedbackMessage: String
            get() = "Sorry, I didn't understand that command"
    }
}
