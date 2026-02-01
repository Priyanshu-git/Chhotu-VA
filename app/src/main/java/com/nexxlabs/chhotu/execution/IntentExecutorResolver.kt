package com.nexxlabs.chhotu.execution

import com.nexxlabs.chhotu.domain.model.CommandIntent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves which executor should handle a given CommandIntent.
 * Separates routing logic from execution logic.
 * 
 * This class is the single point of control for all intent-to-executor routing,
 * making it easy to add priority rules, fallbacks, or special handling.
 */
@Singleton
class IntentExecutorResolver @Inject constructor(
    private val registry: ExecutorRegistry
) {
    
    /**
     * Resolve the appropriate executor for the given intent.
     * 
     * @param intent The command intent to route
     * @return The executor that can handle this intent, or null if none available
     */
    fun resolve(intent: CommandIntent): AppExecutor? {
        return registry.getExecutorFor(intent::class)
    }
    
    /**
     * Execute an intent by resolving its executor and delegating.
     * Returns a failure result if no executor is found.
     * 
     * @param intent The command intent to execute
     * @return ExecutionResult from the appropriate executor
     */
    fun executeIntent(intent: CommandIntent): ExecutionResult {
        val executor = resolve(intent)
        return executor?.execute(intent)
            ?: ExecutionResult.Failure("No executor available for this command")
    }
}
