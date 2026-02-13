package com.nexxlabs.chhotu.domain.engine

import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent

interface EngineInterface {
    suspend fun analyze(command: String): StructuredIntent
}