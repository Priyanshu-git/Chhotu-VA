package com.nexxlabs.chhotu.domain.registry

import com.nexxlabs.chhotu.domain.registry.model.RegistryEntry

interface AppRegistry {
    fun findByAlias(alias: String): RegistryEntry?
}
