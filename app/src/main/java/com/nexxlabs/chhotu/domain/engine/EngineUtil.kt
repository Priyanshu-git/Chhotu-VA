package com.nexxlabs.chhotu.domain.engine

import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent

object EngineUtil {
    fun fallbackIntent(): StructuredIntent {
        return StructuredIntent(
            intentType = IntentType.UNKNOWN,
            targetApp = null,
            action = null,
            entities = emptyMap(),
            confidence = 0.0
        )
    }
}