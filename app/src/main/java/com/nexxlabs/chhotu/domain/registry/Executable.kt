package com.nexxlabs.chhotu.domain.registry

import android.content.Context
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

interface Executable {
    fun execute(
        context: Context,
        entities: Map<String, String>
    ): ExecutionResult
}
