package com.nexxlabs.chhotu.execution

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nexxlabs.chhotu.domain.model.CommandIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Executor for phone call commands.
 * Initiates a call using ACTION_CALL if permission is granted,
 * otherwise falls back to ACTION_DIAL.
 */
@Singleton
class CallExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : AppExecutor {

    override val supportedIntentTypes: Set<KClass<out CommandIntent>> = setOf(
        CommandIntent.CallContact::class
    )

    override fun execute(intent: CommandIntent): ExecutionResult {
        val callIntent = intent as? CommandIntent.CallContact
            ?: return ExecutionResult.Failure("Invalid intent type for CallExecutor")
        
        val phoneNumber = callIntent.phoneNumber 
            ?: return ExecutionResult.Failure("Could not find phone number for ${callIntent.recipientName}")

        return try {
            val phoneUri = Uri.parse("tel:$phoneNumber")
            val intent = Intent(Intent.ACTION_CALL, phoneUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Note: MainActivity should have checked for CALL_PHONE permission.
            // If it fails here, it might be due to missing permission.
            context.startActivity(intent)
            ExecutionResult.Success("Calling ${callIntent.recipientName}")
        } catch (e: Exception) {
            // Fallback to dialer if direct call fails
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                ExecutionResult.Success("Opening dialer for ${callIntent.recipientName}")
            } catch (e2: Exception) {
                ExecutionResult.Failure("Could not initiate call: ${e.message}")
            }
        }
    }
}
