package com.nexxlabs.chhotu.data.contacts

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.nexxlabs.chhotu.domain.model.Contact
import com.nexxlabs.chhotu.domain.repository.ContactRepository
import com.nexxlabs.chhotu.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ContactRepository {

    override fun findContactsByName(name: String): List<Contact> {
        val contacts = getAllContacts()
        return contacts.filter { it.name.contains(name, ignoreCase = true) }
    }

    private fun getAllContacts(): List<Contact> {
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
                val contactName = it.getString(nameIndex) ?: ""
                val number = (it.getString(numberIndex) ?: "").replace(Regex("[^0-9+]"), "")

                if (contactName.isNotBlank() && number.isNotBlank()) {
                    contactList.add(Contact(contactName, number))
                }
            }
        } ?: Log.e(Constants.LOG.INPUT, "Could not query contacts cursor is null")

        return deduplicateContacts(contactList)
    }

    companion object {
        internal fun normalizeNumber(number: String): String {
            val digits = number.removePrefix("+")
            if (digits.length > 10) {
                val localPart = digits.takeLast(10)
                if (localPart.length == 10) return localPart
            }
            return digits
        }

        internal fun deduplicateContacts(contacts: List<Contact>): List<Contact> {
            return contacts
                .groupBy { it.name.lowercase() to normalizeNumber(it.phoneNumber) }
                .map { (_, group) -> group.maxBy { it.phoneNumber.length } }
        }
    }
}
