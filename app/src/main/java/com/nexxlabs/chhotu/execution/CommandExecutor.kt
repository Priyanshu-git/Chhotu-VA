package com.nexxlabs.chhotu.execution

import android.util.Log
import com.nexxlabs.chhotu.domain.engine.CapabilityResolver
import com.nexxlabs.chhotu.domain.engine.CommandNormalizer
import com.nexxlabs.chhotu.domain.engine.ai.AIIntentEngine
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central command executor that orchestrates the command processing pipeline.
 *
 * Pipeline:
 * 1. Normalize Input (CommandNormalizer)
 * 2. Extract Intent (AIIntentEngine)
 * 3. Resolve and Execute (CapabilityResolver)
 */
@Singleton
class CommandExecutor @Inject constructor(
    private val commandNormalizer: CommandNormalizer,
    private val aiIntentEngine: AIIntentEngine,
    private val capabilityResolver: CapabilityResolver
) {
    
    /**
     * Execute a raw voice command.
     * 
     * @param rawCommand The raw text from speech recognition
     * @return ExecutionResult
     */
    suspend fun execute(rawCommand: String): ExecutionResult {
        // 1. Normalize
        val normalizedText = commandNormalizer.normalize(rawCommand)
        Log.d(Constants.LOG.DECISION, "Normalized: $normalizedText")
        
        // 2. AI Intent Extraction
        val structuredIntent = aiIntentEngine.analyze(normalizedText)
        Log.d(Constants.LOG.DECISION, "Intent: $structuredIntent")
        
        // 3. Resolve and Execute
        return capabilityResolver.resolveAndExecute(structuredIntent)
    }
}
