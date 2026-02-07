package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nexxlabs.chhotu.domain.engine.ContactManager
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

/**
 * Handles system-level actions like calling usually requiring specific URI schemes.
 * Now supports contact resolution.
 */
class SystemExecutable(
    private val action: String,
    private val uriScheme: String? = null, // e.g. "tel" for ACTION_CALL
    private val contactManager: ContactManager? = null
) : Executable {
    override fun execute(context: Context, entities: Map<String, String>): ExecutionResult {
        return try {
            var dataUri: Uri? = null
            
            if (uriScheme != null) {
                // Check if we have a contact name that needs resolution
                val contactName = entities["contact"]
                val contactNumber = if (contactName != null && contactManager != null) {
                   contactManager.findContactByName(contactName)?.phoneNumber
                } else {
                    entities["contact_number"]
                }
                
                if (contactNumber != null) {
                    dataUri = Uri.parse("$uriScheme:$contactNumber")
                } else if (entities.containsKey("contact_number") || entities.containsKey("contact")){
                   // Contact was required but not found/resolved
                   return ExecutionResult.Failure.MissingRequiredEntities
                }
            }

            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = dataUri
                
                // Add extras
                entities.forEach { (key, value) -> 
                    if (key != "contact" && key != "contact_number") {
                         putExtra(key, value)
                    }
                }
                
                if (action == Intent.ACTION_SENDTO && entities.containsKey("text")) {
                     putExtra("sms_body", entities["text"])
                }
            }
            
            // For Call, we need permission check ideally, but "No validation" rule.
            // However, runtime exception will happen if permission missing.
            // The prompt says "Execution only".
            
            context.startActivity(intent)
            ExecutionResult.Success
        } catch (e: Exception) {
            ExecutionResult.Failure.ExecutionException(e)
        }
    }
}
