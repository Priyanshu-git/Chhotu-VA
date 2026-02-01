package com.nexxlabs.chhotu.domain.engine

import android.util.Log
import com.nexxlabs.chhotu.domain.model.CommandIntent
import com.nexxlabs.chhotu.domain.model.SupportedApp
import com.nexxlabs.chhotu.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rule-based decision engine for routing voice commands to appropriate actions.
 * 
 * Design principles:
 * - Uses deterministic keyword matching (no ML/LLM)
 * - Priority-ordered rules (more specific matches first)
 * - Simple string operations only
 * - Easily extensible for new command types
 * 
 * Rule priority order:
 * 1. SEARCH (most specific - requires "search" + "google")
 * 2. PLAY_MUSIC (requires "play" + music-related keywords)
 * 3. SEND_WHATSAPP (requires "send"/"message" + "whatsapp" + message content)
 * 4. OPEN_APP (fallback for app keywords with "open")
 * 5. UNKNOWN (no match)
 */
@Singleton
class RuleBasedDecisionEngine @Inject constructor(
    private val contactManager: ContactManager
) {
    
    /**
     * Route a normalized command to the appropriate CommandIntent.
     * 
     * @param normalizedInput Text that has been processed by CommandNormalizer
     * @return The matched CommandIntent with extracted parameters
     */
    fun decide(normalizedInput: String): CommandIntent {
        val result = when {
            isSearchCommand(normalizedInput) -> parseSearchCommand(normalizedInput)
            isPlayMusicCommand(normalizedInput) -> parsePlayMusicCommand(normalizedInput)
            isCallCommand(normalizedInput) -> parseCallCommand(normalizedInput)
            isSMSCommand(normalizedInput) -> parseSMSCommand(normalizedInput)
            isSendWhatsAppCommand(normalizedInput) -> parseSendWhatsAppCommand(normalizedInput)
            isOpenAppCommand(normalizedInput) -> parseOpenAppCommand(normalizedInput)
            else -> CommandIntent.Unknown(normalizedInput)
        }
        Log.d(Constants.LOG.DECISION, "Decided: ${result::class.simpleName} for input: $normalizedInput")
        return result
    }
    
    // ==================== SEARCH COMMAND ====================
    
    private fun isSearchCommand(input: String): Boolean {
        return input.contains("search") && input.contains("google")
    }
    
    private fun parseSearchCommand(input: String): CommandIntent {
        // Pattern: "search <query> on google" or "search for <query> on google"
        val query = input
            .replace("on google", "")
            .replace("search for", "")
            .replace("search", "")
            .trim()
        
        return if (query.isNotBlank()) {
            CommandIntent.Search(query)
        } else {
            CommandIntent.Unknown(input)
        }
    }
    
    // ==================== PLAY MUSIC COMMAND ====================
    
    private fun isPlayMusicCommand(input: String): Boolean {
        return input.contains("play") && 
               (input.contains("youtube music") || input.contains("music"))
    }
    
    private fun parsePlayMusicCommand(input: String): CommandIntent {
        // Pattern: "play <artist/song> on youtube music"
        val query = input
            .replace("on youtube music", "")
            .replace("youtube music", "")
            .replace("on music", "")
            .replace("play", "")
            .trim()
        
        return if (query.isNotBlank()) {
            CommandIntent.PlayMusic(query)
        } else {
            CommandIntent.Unknown(input)
        }
    }

    // ==================== CALL COMMAND ====================

    private fun isCallCommand(input: String): Boolean {
        return input.startsWith("call ")
    }

    private fun parseCallCommand(input: String): CommandIntent {
        // Pattern: "call <name>"
        val name = input.replace("call ", "").trim()
        if (name.isBlank()) return CommandIntent.Unknown(input)

        val contact = contactManager.findContactByName(name)
        return CommandIntent.CallContact(name, contact?.phoneNumber)
    }

    // ==================== SMS COMMAND ====================

    private fun isSMSCommand(input: String): Boolean {
        return (input.contains("text") || input.contains("sms")) && 
               (input.contains("saying") || input.contains("say"))
    }

    private fun parseSMSCommand(input: String): CommandIntent {
        // Pattern: "text <name> saying <message>"
        // Pattern: "send sms to <name> saying <message>"
        
        val messageStartIndicators = listOf("saying ", "say ")
        var message = ""
        var recipientName = ""
        
        for (indicator in messageStartIndicators) {
            val indicatorIndex = input.indexOf(indicator)
            if (indicatorIndex != -1) {
                message = input.substring(indicatorIndex + indicator.length).trim()
                
                val subBeforeMessage = input.substring(0, indicatorIndex).trim()
                recipientName = subBeforeMessage
                    .replace("send sms to", "")
                    .replace("text to", "")
                    .replace("text", "")
                    .replace("sms", "")
                    .trim()
                break
            }
        }

        return if (message.isNotBlank() && recipientName.isNotBlank()) {
            val contact = contactManager.findContactByName(recipientName)
            CommandIntent.SendSMS(message, recipientName, contact?.phoneNumber)
        } else {
            CommandIntent.Unknown(input)
        }
    }
    
    // ==================== WHATSAPP MESSAGE COMMAND ====================
    
    private fun isSendWhatsAppCommand(input: String): Boolean {
        val hasSendKeyword = input.contains("send") || input.contains("message")
        val hasWhatsApp = input.contains("whatsapp") || input.contains("whats app")

        return hasSendKeyword && hasWhatsApp
    }
    
    private fun parseSendWhatsAppCommand(input: String): CommandIntent {
        // Pattern: "send message to <recipient> on whatsapp saying <message>"
        // We extract the message after "saying" or similar keywords
        
        val messageStartIndicators = listOf("saying ", "say ", "that ")
        var message = ""
        var recipientName: String? = null
        
        for (indicator in messageStartIndicators) {
            val indicatorIndex = input.indexOf(indicator)
            if (indicatorIndex != -1) {
                message = input.substring(indicatorIndex + indicator.length).trim()
                
                // Try to extract recipient (text between "to" and "on whatsapp")
                val toIndex = input.indexOf(" to ")
                val onWhatsAppIndex = input.indexOf("on whatsapp")
                if (onWhatsAppIndex == -1) input.indexOf("on whats app")
                
                if (toIndex != -1 && onWhatsAppIndex != -1 && toIndex < onWhatsAppIndex) {
                    recipientName = input.substring(toIndex + 4, onWhatsAppIndex).trim()
                }
                break
            }
        }
        
        return if (message.isNotBlank()) {
            val phoneNumber = recipientName?.let { contactManager.findContactByName(it)?.phoneNumber }
            CommandIntent.SendWhatsAppMessage(message, recipientName, phoneNumber)
        } else {
            CommandIntent.Unknown(input)
        }
    }
    
    // ==================== OPEN APP COMMAND ====================
    
    private fun isOpenAppCommand(input: String): Boolean {
        return input.contains("open") && SupportedApp.fromKeyword(input) != null
    }
    
    private fun parseOpenAppCommand(input: String): CommandIntent {
        val app = SupportedApp.fromKeyword(input)
        return if (app != null) {
            CommandIntent.OpenApp(app)
        } else {
            CommandIntent.Unknown(input)
        }
    }
}

