package com.nexxlabs.chhotu.domain.usecase

import com.nexxlabs.chhotu.domain.engine.CommandNormalizer
import com.nexxlabs.chhotu.domain.engine.EngineInterface
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ExecuteVoiceCommandUseCase @Inject constructor(
    private val commandNormalizer: CommandNormalizer,
    @Named("basic") private val basicEngine: EngineInterface,
    @Named("ai") private val aiEngine: EngineInterface,
    private val capabilityResolver: com.nexxlabs.chhotu.domain.engine.CapabilityResolver
) {
    suspend fun execute(rawCommand: String): CommandResult {
        val normalizedText = commandNormalizer.normalize(rawCommand)

        var intent = basicEngine.analyze(normalizedText)
        if (intent.intentType == IntentType.UNKNOWN) {
            intent = aiEngine.analyze(normalizedText)
        }

        return capabilityResolver.resolveAndExecute(intent)
    }

    fun executeIntent(intent: StructuredIntent): CommandResult {
        return capabilityResolver.resolveAndExecute(intent)
    }
}
