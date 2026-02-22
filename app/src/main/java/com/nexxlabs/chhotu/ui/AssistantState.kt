package com.nexxlabs.chhotu.ui

import com.nexxlabs.chhotu.domain.model.Contact
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent

/**
 * Represents the different states of the assistant UI.
 */
sealed class AssistantState {
    data object Idle : AssistantState()
    data object Listening : AssistantState()
    data class Processing(val recognizedText: String) : AssistantState()
    data class Success(
        val originalCommand: String,
        val feedbackMessage: String
    ) : AssistantState()

    data class SelectContact(
        val contacts: List<Contact>,
        val originalCommand: String,
        val intent: StructuredIntent
    ) : AssistantState()

    data class Error(
        val originalCommand: String?,
        val errorMessage: String
    ) : AssistantState()
}
