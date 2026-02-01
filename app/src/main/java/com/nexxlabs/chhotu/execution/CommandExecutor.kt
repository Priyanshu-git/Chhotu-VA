package com.nexxlabs.chhotu.execution

import com.nexxlabs.chhotu.domain.model.CommandIntent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central command executor that routes CommandIntent to appropriate executors.
 * Acts as a facade over the IntentExecutorResolver.
 * 
 * This class is now lightweight - all routing logic is delegated to the resolver,
 * and executors are managed by the registry via Hilt multibinding.
 */
@Singleton
class CommandExecutor @Inject constructor(
    private val resolver: IntentExecutorResolver
) {
    
    /**
     * Execute a command intent using the appropriate executor.
     * 
     * @param intent The command to execute
     * @return ExecutionResult with feedback message
     */
    fun execute(intent: CommandIntent): ExecutionResult {
        // Handle Unknown intents directly - no executor needed
        if (intent is CommandIntent.Unknown) {
            return ExecutionResult.Failure(intent.feedbackMessage)
        }
        
        return resolver.executeIntent(intent)
    }
}
