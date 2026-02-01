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
     * Search for a contact by name.
     * Returns the first match or null if not found.
     * Uses simple case-insensitive matching.
     */
    fun findContactByName(name: String): Contact? {
        val contacts = getAllContacts()
        return contacts.firstOrNull { it.name.contains(name, ignoreCase = true) }
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
        
        return contactList
    }
}
