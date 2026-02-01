package com.nexxlabs.chhotu.execution

import com.nexxlabs.chhotu.domain.model.CommandIntent
import kotlin.reflect.KClass

/**
 * Interface for all app executors in the system.
 * Executors handle specific command intents and return execution results.
 * 
 * This interface enables:
 * - Scalable executor registration via Hilt multibinding
 * - Uniform handling of all command types
 * - Easy addition of new executors without modifying existing code
 */
interface AppExecutor {
    
    /**
     * The set of CommandIntent types this executor can handle.
     * Used by the registry to route intents to the correct executor.
     */
    val supportedIntentTypes: Set<KClass<out CommandIntent>>
    
    /**
     * Execute the given command intent.
     * 
     * @param intent The command to execute (must be one of supportedIntentTypes)
     * @return ExecutionResult indicating success or failure
     */
    fun execute(intent: CommandIntent): ExecutionResult
}
