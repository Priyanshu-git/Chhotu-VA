package com.nexxlabs.chhotu.data.local

/**
 * Represents a command in the history.
 * Persisted via DataStore as JSON.
 */
data class CommandHistoryItem(
    val originalText: String,
    val intentType: String,
    val wasSuccessful: Boolean,
    val feedbackMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
