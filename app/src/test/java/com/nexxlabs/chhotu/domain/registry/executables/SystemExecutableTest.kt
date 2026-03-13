package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Intent
import com.nexxlabs.chhotu.domain.model.Contact
import com.nexxlabs.chhotu.domain.platform.IntentLauncher
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.domain.repository.ContactRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SystemExecutableTest {

    private lateinit var intentLauncher: IntentLauncher
    private lateinit var contactRepository: ContactRepository

    @Before
    fun setup() {
        intentLauncher = mockk(relaxed = true)
        contactRepository = mockk()
        every { intentLauncher.launchUriWithExtras(any(), any(), any()) } returns ExecutionResult.Success
    }

    @Test
    fun `uses contact_number directly when provided`() {
        val executable = SystemExecutable(Intent.ACTION_CALL, "tel", contactRepository, intentLauncher)
        val entities = mapOf("contact" to "John", "contact_number" to "+919876543210")

        val result = executable.execute(entities)

        assertEquals(ExecutionResult.Success, result)
        verify { intentLauncher.launchUriWithExtras(Intent.ACTION_CALL, "tel:+919876543210", any()) }
    }

    @Test
    fun `resolves contact by name when no number provided`() {
        val executable = SystemExecutable(Intent.ACTION_CALL, "tel", contactRepository, intentLauncher)
        every { contactRepository.findContactsByName("John") } returns listOf(
            Contact("John Doe", "+919876543210")
        )

        val result = executable.execute(mapOf("contact" to "John"))

        assertEquals(ExecutionResult.Success, result)
        verify { intentLauncher.launchUriWithExtras(Intent.ACTION_CALL, "tel:+919876543210", any()) }
    }

    @Test
    fun `returns AmbiguousContact when multiple contacts found`() {
        val executable = SystemExecutable(Intent.ACTION_CALL, "tel", contactRepository, intentLauncher)
        val contacts = listOf(
            Contact("John A", "111"),
            Contact("John B", "222")
        )
        every { contactRepository.findContactsByName("John") } returns contacts

        val result = executable.execute(mapOf("contact" to "John"))

        assertTrue(result is ExecutionResult.Failure.AmbiguousContact)
        assertEquals(contacts, (result as ExecutionResult.Failure.AmbiguousContact).contacts)
    }

    @Test
    fun `returns MissingRequiredEntities when contact not found`() {
        val executable = SystemExecutable(Intent.ACTION_CALL, "tel", contactRepository, intentLauncher)
        every { contactRepository.findContactsByName("Nobody") } returns emptyList()

        val result = executable.execute(mapOf("contact" to "Nobody"))

        assertEquals(ExecutionResult.Failure.MissingRequiredEntities, result)
    }
}
