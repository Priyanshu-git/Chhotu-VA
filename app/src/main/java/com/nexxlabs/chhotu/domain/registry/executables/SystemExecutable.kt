package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Intent
import com.nexxlabs.chhotu.domain.constants.EntityKeys
import com.nexxlabs.chhotu.domain.platform.IntentLauncher
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.domain.repository.ContactRepository

class SystemExecutable(
    private val action: String,
    private val uriScheme: String? = null,
    private val contactRepository: ContactRepository? = null,
    private val intentLauncher: IntentLauncher
) : Executable {

    override fun execute(entities: Map<String, String>): ExecutionResult {
        return try {
            var uriString: String? = null

            if (uriScheme != null) {
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

                if (contactNumber != null) {
                    uriString = "$uriScheme:$contactNumber"
                } else if (entities.containsKey(EntityKeys.CONTACT_NUMBER) || entities.containsKey(EntityKeys.CONTACT)) {
                    return ExecutionResult.Failure.MissingRequiredEntities
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
