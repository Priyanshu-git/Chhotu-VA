package com.nexxlabs.chhotu.execution

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.nexxlabs.chhotu.domain.engine.CapabilityResolver
import com.nexxlabs.chhotu.domain.model.CommandIntent
import com.nexxlabs.chhotu.domain.model.SupportedApp
import com.nexxlabs.chhotu.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Executor for WhatsApp message commands.
 * Opens WhatsApp share intent with message text.
 * User must manually confirm sending - no silent automation.
 */
@Singleton
class WhatsAppExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val capabilityResolver: CapabilityResolver
) : AppExecutor {
    
    override val supportedIntentTypes: Set<KClass<out CommandIntent>> = setOf(
        CommandIntent.SendWhatsAppMessage::class
    )
    
    override fun execute(intent: CommandIntent): ExecutionResult {
        val whatsAppIntent = intent as? CommandIntent.SendWhatsAppMessage
            ?: return ExecutionResult.Failure("Invalid intent type for WhatsAppExecutor")
        return executeSendMessage(whatsAppIntent.message, whatsAppIntent.phoneNumber)
    }
    
    /**
     * Execute WhatsApp share intent with the given message.
     * 
     * @param message The message content to share
     * @param phoneNumber Optional phone number to target a specific contact
     * @return ExecutionResult indicating success or failure
     */
    private fun executeSendMessage(message: String, phoneNumber: String?): ExecutionResult {
        if (!capabilityResolver.isAppInstalled(SupportedApp.WHATSAPP)) {
            return ExecutionResult.AppNotInstalled(SupportedApp.WHATSAPP.displayName)
        }
        
        return try {
            val intent = if (phoneNumber != null) {
                // Direct message to specific number (number must include country code without + or 00)
                val cleanNumber = phoneNumber.replace("+", "").replace("^00", "")
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}")
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(SupportedApp.WHATSAPP.packageName)
                }
            } else {
                // General share intent
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage(SupportedApp.WHATSAPP.packageName)
                    putExtra(Intent.EXTRA_TEXT, message)
                }
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ExecutionResult.Success("Opening WhatsApp to send your message")
        } catch (e: ActivityNotFoundException) {
            Log.e(Constants.LOG.EXECUTOR, "WhatsApp app not found", e)
            ExecutionResult.AppNotInstalled(SupportedApp.WHATSAPP.displayName)
        } catch (e: Exception) {
            ExecutionResult.Failure(e.message ?: "Unknown error")
        }
    }
}
