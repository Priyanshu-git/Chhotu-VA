package com.nexxlabs.chhotu.execution

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nexxlabs.chhotu.domain.engine.CapabilityResolver
import com.nexxlabs.chhotu.domain.model.SupportedApp
import com.nexxlabs.chhotu.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executor for WhatsApp message commands.
 * Opens WhatsApp share intent with message text.
 * User must manually confirm sending - no silent automation.
 */
@Singleton
class WhatsAppExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val capabilityResolver: CapabilityResolver
) {
    
    /**
     * Execute WhatsApp share intent with the given message.
     * 
     * @param message The message content to share
     * @return ExecutionResult indicating success or failure
     */
    fun execute(message: String): ExecutionResult {
        if (!capabilityResolver.isAppInstalled(SupportedApp.WHATSAPP)) {
            return ExecutionResult.AppNotInstalled(SupportedApp.WHATSAPP.displayName)
        }
        
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage(SupportedApp.WHATSAPP.packageName)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
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
