package com.nexxlabs.chhotu.execution

import com.nexxlabs.chhotu.domain.model.CommandIntent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Registry holding all available executors in the system.
 * Uses Hilt's Set multibinding to automatically collect all AppExecutor implementations.
 * 
 * Benefits:
 * - O(1) executor lookup by intent type
 * - Self-maintaining: new executors auto-register via @IntoSet
 * - No constructor bloat as app count grows
 */
@Singleton
class ExecutorRegistry @Inject constructor(
    executors: Set<@JvmSuppressWildcards AppExecutor>
) {
    
    /**
     * Map from intent type to the executor that handles it.
     * Built once at construction time from the injected executor set.
     */
    private val executorMap: Map<KClass<out CommandIntent>, AppExecutor> = buildMap {
        executors.forEach { executor ->
            executor.supportedIntentTypes.forEach { intentType ->
                put(intentType, executor)
            }
        }
    }
    
    /**
     * Find the executor that handles the given intent type.
     * 
     * @param intentType The KClass of the CommandIntent to look up
     * @return The executor that handles this intent type, or null if none registered
     */
    fun getExecutorFor(intentType: KClass<out CommandIntent>): AppExecutor? {
        return executorMap[intentType]
    }
    
    /**
     * Check if an executor is registered for the given intent type.
     */
    fun hasExecutorFor(intentType: KClass<out CommandIntent>): Boolean {
        return intentType in executorMap
    }
}
