package com.nexxlabs.chhotu.domain.usecase

import com.nexxlabs.chhotu.domain.constants.ActionIds
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackMessageGeneratorTest {

    private val generator = FeedbackMessageGenerator()

    @Test
    fun `generates open message with app name`() {
        val result = CommandResult(ExecutionResult.Success, displayName = "WhatsApp", actionId = ActionIds.OPEN)
        assertEquals("Opening WhatsApp.", generator.generate(result))
    }

    @Test
    fun `generates search message with app name`() {
        val result = CommandResult(ExecutionResult.Success, displayName = "YouTube", actionId = ActionIds.SEARCH)
        assertEquals("Searching on YouTube.", generator.generate(result))
    }

    @Test
    fun `generates call message`() {
        val result = CommandResult(ExecutionResult.Success, displayName = "Phone", actionId = ActionIds.CALL)
        assertEquals("Calling via Phone.", generator.generate(result))
    }

    @Test
    fun `generates volume increased message`() {
        val result = CommandResult(ExecutionResult.Success, actionId = ActionIds.INCREASE)
        assertEquals("Volume increased.", generator.generate(result))
    }

    @Test
    fun `generates app not installed message`() {
        val result = CommandResult(ExecutionResult.Failure.AppNotInstalled, displayName = "Spotify")
        assertEquals("Spotify is not installed on your device.", generator.generate(result))
    }

    @Test
    fun `generates action not supported message with name`() {
        val result = CommandResult(ExecutionResult.Failure.ActionNotSupported, displayName = "Clock")
        assertEquals("I can't do that with Clock.", generator.generate(result))
    }

    @Test
    fun `generates action not supported message without name`() {
        val result = CommandResult(ExecutionResult.Failure.ActionNotSupported)
        assertEquals("I can't do that yet.", generator.generate(result))
    }

    @Test
    fun `generates ambiguous contact message`() {
        val result = CommandResult(ExecutionResult.Failure.AmbiguousContact(emptyList()))
        assertEquals("Multiple contacts found. Which one would you like to use?", generator.generate(result))
    }

    @Test
    fun `generates execution exception message`() {
        val result = CommandResult(ExecutionResult.Failure.ExecutionException(RuntimeException("Network error")))
        assertEquals("Something went wrong: Network error", generator.generate(result))
    }

    @Test
    fun `generates default done message for unknown action`() {
        val result = CommandResult(ExecutionResult.Success, actionId = "CUSTOM_ACTION")
        assertEquals("Done.", generator.generate(result))
    }
}
