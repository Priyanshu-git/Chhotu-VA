package com.nexxlabs.chhotu.domain.registry.model

import com.nexxlabs.chhotu.domain.registry.Executable

data class Action(
    val id: String,
    val aliases: Set<String> = emptySet(),
    val contract: ActionContract,
    val primaryExecutable: Executable,
    val fallbackExecutable: Executable? = null
)
