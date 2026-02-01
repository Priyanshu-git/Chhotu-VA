package com.nexxlabs.chhotu.execution

import android.content.Context
import android.telephony.SmsManager
import com.nexxlabs.chhotu.domain.model.CommandIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Executor for SMS commands.
 * Sends SMS messages directly using SmsManager.
 */
@Singleton
class SMSExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : AppExecutor {

    override val supportedIntentTypes: Set<KClass<out CommandIntent>> = setOf(
        CommandIntent.SendSMS::class
    )

    override fun execute(intent: CommandIntent): ExecutionResult {
        val smsIntent = intent as? CommandIntent.SendSMS
            ?: return ExecutionResult.Failure("Invalid intent type for SMSExecutor")
            
        val phoneNumber = smsIntent.phoneNumber
            ?: return ExecutionResult.Failure("Could not find phone number for ${smsIntent.recipientName}")

        return try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, smsIntent.message, null, null)
            ExecutionResult.Success("SMS sent to ${smsIntent.recipientName}")
        } catch (e: Exception) {
            ExecutionResult.Failure("Failed to send SMS: ${e.message}")
        }
    }
}
