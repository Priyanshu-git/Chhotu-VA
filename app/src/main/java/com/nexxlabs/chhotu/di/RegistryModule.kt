package com.nexxlabs.chhotu.di

import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.domain.registry.StaticAppRegistry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RegistryModule {

    @Binds
    @Singleton
    abstract fun bindAppRegistry(
        staticAppRegistry: StaticAppRegistry
    ): AppRegistry
}
