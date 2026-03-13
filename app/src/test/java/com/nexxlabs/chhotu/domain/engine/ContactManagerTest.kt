package com.nexxlabs.chhotu.domain.engine

import com.nexxlabs.chhotu.domain.model.Contact
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactManagerTest {

    @Test
    fun `deduplicates contacts with same name and number differing only by country code`() {
        val contacts = listOf(
            Contact("Paul", "+919876543210"),
            Contact("Paul", "9876543210")
        )

        val result = ContactManager.deduplicateContacts(contacts)

        assertEquals(1, result.size)
        assertEquals("+919876543210", result.first().phoneNumber)
    }

    @Test
    fun `keeps distinct contacts with same name but different numbers`() {
        val contacts = listOf(
            Contact("Paul", "+919876543210"),
            Contact("Paul", "+919999999999")
        )

        val result = ContactManager.deduplicateContacts(contacts)

        assertEquals(2, result.size)
    }

    @Test
    fun `keeps distinct contacts with different names`() {
        val contacts = listOf(
            Contact("Paul", "+919876543210"),
            Contact("Paula", "+919876543210")
        )

        val result = ContactManager.deduplicateContacts(contacts)

        assertEquals(2, result.size)
    }

    @Test
    fun `deduplication prefers number with country code`() {
        val contacts = listOf(
            Contact("paul", "9876543210"),
            Contact("Paul", "+919876543210")
        )

        val result = ContactManager.deduplicateContacts(contacts)

        assertEquals(1, result.size)
        assertEquals("+919876543210", result.first().phoneNumber)
    }

    @Test
    fun `normalizeNumber strips country code for numbers longer than 10 digits`() {
        assertEquals("9876543210", ContactManager.normalizeNumber("+919876543210"))
        assertEquals("9876543210", ContactManager.normalizeNumber("919876543210"))
        assertEquals("9876543210", ContactManager.normalizeNumber("9876543210"))
    }

    @Test
    fun `normalizeNumber preserves short numbers`() {
        assertEquals("12345", ContactManager.normalizeNumber("12345"))
        assertEquals("12345", ContactManager.normalizeNumber("+12345"))
    }
}
