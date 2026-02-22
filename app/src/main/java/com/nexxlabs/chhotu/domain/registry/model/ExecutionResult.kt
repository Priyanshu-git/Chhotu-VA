package com.nexxlabs.chhotu.domain.registry.model

import com.nexxlabs.chhotu.domain.model.Contact

sealed class ExecutionResult {
    object Success : ExecutionResult()

    sealed class Failure : ExecutionResult() {
        object AppNotInstalled : Failure()
        object ActionNotSupported : Failure()
        object MissingRequiredEntities : Failure()
        data class AmbiguousContact(val contacts: List<Contact>) : Failure()
        data class ExecutionException(val throwable: Throwable) : Failure()
    }
}
