package com.nexxlabs.chhotu.domain.registry.model

/**
 * Wraps the execution result with context about the app and action.
 * This allows the UI to generate rich, contextual feedback messages.
 */
data class CommandResult(
    val executionResult: ExecutionResult,
    val displayName: String? = null,
    val actionId: String? = null
)
