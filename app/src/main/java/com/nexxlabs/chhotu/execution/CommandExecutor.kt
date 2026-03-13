package com.nexxlabs.chhotu.execution

import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.usecase.ExecuteVoiceCommandUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central command executor. Delegates to ExecuteVoiceCommandUseCase.
 * Kept as a thin facade for backward compatibility.
 */
@Singleton
class CommandExecutor @Inject constructor(
    private val executeVoiceCommandUseCase: ExecuteVoiceCommandUseCase
) {
    suspend fun execute(rawCommand: String): CommandResult {
        return executeVoiceCommandUseCase.execute(rawCommand)
    }

    fun executeIntent(intent: StructuredIntent): CommandResult {
        return executeVoiceCommandUseCase.executeIntent(intent)
    }
}
