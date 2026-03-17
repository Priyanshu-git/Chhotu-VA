package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Intent
import android.net.Uri
import com.nexxlabs.chhotu.domain.constants.EntityKeys
import com.nexxlabs.chhotu.domain.platform.IntentLauncher
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.domain.repository.ContactRepository

class SystemExecutable(
    private val action: String,
    private val uriScheme: String? = null,
    private val contactRepository: ContactRepository? = null,
    private val intentLauncher: IntentLauncher,
    private val uriTemplate: String? = null
) : Executable {

    override fun execute(entities: Map<String, String>): ExecutionResult {
        return try {
            var uriString: String? = null

            if (uriScheme != null || uriTemplate != null) {
                val contactName = entities[EntityKeys.CONTACT]
                val contactNumber = if (entities.containsKey(EntityKeys.CONTACT_NUMBER)) {
                    entities[EntityKeys.CONTACT_NUMBER]
                } else if (contactName != null && contactRepository != null) {
                    val foundContacts = contactRepository.findContactsByName(contactName)
                    when {
                        foundContacts.isEmpty() -> null
                        foundContacts.size == 1 -> foundContacts.first().phoneNumber
                        else -> return ExecutionResult.Failure.AmbiguousContact(foundContacts)
                    }
                } else {
                    null
                }

                if (uriTemplate != null) {
                    if (contactNumber == null && (entities.containsKey(EntityKeys.CONTACT) || entities.containsKey(EntityKeys.CONTACT_NUMBER))) {
                        return ExecutionResult.Failure.MissingRequiredEntities
                    }
                    val resolved = entities.toMutableMap()
                    if (contactNumber != null) resolved[EntityKeys.PHONE] = contactNumber
                    var finalUri:String = uriTemplate
                    resolved.forEach { (key, value) -> finalUri = finalUri.replace("{$key}", Uri.encode(value)) }
                    uriString = finalUri
                } else if (uriScheme != null) {
                    if (contactNumber != null) {
                        uriString = "$uriScheme:$contactNumber"
                    } else if (entities.containsKey(EntityKeys.CONTACT_NUMBER) || entities.containsKey(EntityKeys.CONTACT)) {
                        return ExecutionResult.Failure.MissingRequiredEntities
                    }
                }
            }

            val extras = entities.filterKeys {
                it != EntityKeys.CONTACT && it != EntityKeys.CONTACT_NUMBER
            }.toMutableMap()

            if (action == Intent.ACTION_SENDTO && entities.containsKey(EntityKeys.TEXT)) {
                extras["sms_body"] = entities[EntityKeys.TEXT]!!
            }

            if (uriString != null) {
                intentLauncher.launchUriWithExtras(action, uriString, extras)
            } else {
                intentLauncher.launchIntent(action, extras = extras)
            }
        } catch (e: Exception) {
            ExecutionResult.Failure.ExecutionException(e)
        }
    }
}
