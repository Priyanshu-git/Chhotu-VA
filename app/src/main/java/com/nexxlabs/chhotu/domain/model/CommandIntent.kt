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
     * @param recipientName Optional recipient name extracted from command
     * @param phoneNumber Optional resolved phone number
     */
    data class SendWhatsAppMessage(
        val message: String,
        val recipientName: String? = null,
        val phoneNumber: String? = null
    ) : CommandIntent() {
        val feedbackMessage: String
            get() = if (recipientName != null) {
                "Opening WhatsApp to send message to $recipientName"
            } else {
                "Opening WhatsApp to send your message"
            }
    }
    
    /**
     * Initiate a phone call.
     * @param recipientName The name of the person to call
     * @param phoneNumber The resolved phone number
     */
    data class CallContact(
        val recipientName: String,
        val phoneNumber: String? = null
    ) : CommandIntent() {
        val feedbackMessage: String
            get() = if (phoneNumber != null) {
                "Calling $recipientName"
            } else {
                "Sorry, I couldn't find a phone number for $recipientName"
            }
    }

    /**
     * Send an SMS message.
     * @param message The message content
     * @param recipientName The name of the person to text
     * @param phoneNumber The resolved phone number
     */
    data class SendSMS(
        val message: String,
        val recipientName: String,
        val phoneNumber: String? = null
    ) : CommandIntent() {
        val feedbackMessage: String
            get() = if (phoneNumber != null) {
                "Sending SMS to $recipientName"
            } else {
                "Sorry, I couldn't find a phone number for $recipientName"
            }
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
