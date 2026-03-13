package com.nexxlabs.chhotu.domain.engine

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.nexxlabs.chhotu.domain.model.Contact
import com.nexxlabs.chhotu.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages access to device contacts.
 * Provides methods to retrieve and search for contacts by name.
 */
@Singleton
class ContactManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Search for contacts by name.
     * Returns all matches that contain the query string.
     * Uses simple case-insensitive matching.
     */
    fun findContactsByName(name: String): List<Contact> {
        val contacts = getAllContacts()
        return contacts.filter { it.name.contains(name, ignoreCase = true) }
    }
    
    /**
     * Get all contacts with phone numbers from the device.
     */
    fun getAllContacts(): List<Contact> {
        val contactList = mutableListOf<Contact>()
        val contentResolver = context.contentResolver
        
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: ""
                var number = it.getString(numberIndex) ?: ""
                
                // Clean up phone number (remove spaces, dashes, etc.)
                number = number.replace(Regex("[^0-9+]"), "")
                
                if (name.isNotBlank() && number.isNotBlank()) {
                    contactList.add(Contact(name, number))
                }
            }
        } ?: Log.e(Constants.LOG.INPUT, "Could not query contacts cursor is null")

        return deduplicateContacts(contactList)
    }

    companion object {
        /**
         * Strips country code prefix to get the local number for comparison.
         * E.g. "+919876543210" and "9876543210" both yield "9876543210".
         */
        internal fun normalizeNumber(number: String): String {
            val digits = number.removePrefix("+")
            // Remove country code prefix (1-3 digits) if the remaining part is 10 digits
            if (digits.length > 10) {
                val localPart = digits.takeLast(10)
                if (localPart.length == 10) return localPart
            }
            return digits
        }

        /**
         * Deduplicates contacts that share the same name and underlying phone number.
         * When duplicates exist, keeps the entry with the country code (longest number).
         */
        internal fun deduplicateContacts(contacts: List<Contact>): List<Contact> {
            return contacts
                .groupBy { it.name.lowercase() to normalizeNumber(it.phoneNumber) }
                .map { (_, group) -> group.maxBy { it.phoneNumber.length } }
        }
    }
}
