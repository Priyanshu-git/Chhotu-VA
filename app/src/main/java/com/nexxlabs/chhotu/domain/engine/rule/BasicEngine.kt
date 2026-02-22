package com.nexxlabs.chhotu.domain.engine.rule

import com.nexxlabs.chhotu.domain.engine.EngineInterface
import com.nexxlabs.chhotu.domain.engine.EngineUtil
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent

class BasicEngine() : EngineInterface {

    override suspend fun analyze(command: String): StructuredIntent {
        val tokens = command.split(" ")
        if (tokens.size < 2) return EngineUtil.fallbackIntent()

        val command = tokens[0]
        val parameter = tokens.subList(1, tokens.size).joinToString(" ")
        when (command) {
            "open", "launch", "start" -> {
                return StructuredIntent(
                    intentType = IntentType.OPEN_APP,
                    targetApp = parameter,
                    action = null,
                    entities = emptyMap(),
                    confidence = 1.0
                )
            }

            "call" -> {
                return StructuredIntent(
                    intentType = IntentType.SYSTEM_ACTION,
                    targetApp = "Phone",
                    action = "call",
                    entities = mapOf("contact" to parameter),
                    confidence = 1.0
                )
            }

            else -> {
                return EngineUtil.fallbackIntent()
            }
        }
    }
}