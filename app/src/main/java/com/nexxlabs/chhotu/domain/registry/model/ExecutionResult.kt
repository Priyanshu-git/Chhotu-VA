package com.nexxlabs.chhotu.domain.registry.model

sealed class ExecutionResult {
    object Success : ExecutionResult()

    sealed class Failure : ExecutionResult() {
        object AppNotInstalled : Failure()
        object ActionNotSupported : Failure()
        object MissingRequiredEntities : Failure()
        data class ExecutionException(val throwable: Throwable) : Failure()
    }
}
