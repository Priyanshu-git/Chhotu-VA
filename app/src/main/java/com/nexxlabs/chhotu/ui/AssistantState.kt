package com.nexxlabs.chhotu.ui

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

    data class Error(
        val originalCommand: String?,
        val errorMessage: String
    ) : AssistantState()
}
