package com.nexxlabs.chhotu.domain.registry

import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

interface Executable {
    fun execute(entities: Map<String, String>): ExecutionResult
}
