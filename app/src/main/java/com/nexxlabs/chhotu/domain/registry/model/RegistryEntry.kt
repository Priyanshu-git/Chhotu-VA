package com.nexxlabs.chhotu.domain.registry.model

data class RegistryEntry(
    val appId: String,
    val displayName: String,
    val packageName: String?, // null for system
    val aliases: Set<String>,
    val actions: Set<Action>
)
