package com.nexxlabs.chhotu.di

import com.nexxlabs.chhotu.data.contacts.ContactRepositoryImpl
import com.nexxlabs.chhotu.data.platform.AndroidAppInstallationChecker
import com.nexxlabs.chhotu.data.platform.AndroidIntentLauncher
import com.nexxlabs.chhotu.data.platform.AndroidSystemServiceProvider
import com.nexxlabs.chhotu.domain.engine.EngineInterface
import com.nexxlabs.chhotu.domain.engine.ai.AIIntentEngine
import com.nexxlabs.chhotu.domain.engine.rule.BasicEngine
import com.nexxlabs.chhotu.domain.platform.AppInstallationChecker
import com.nexxlabs.chhotu.domain.platform.IntentLauncher
import com.nexxlabs.chhotu.domain.platform.SystemServiceProvider
import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.domain.registry.StaticAppRegistry
import com.nexxlabs.chhotu.domain.repository.ContactRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RegistryModule {

    @Binds
    @Singleton
    abstract fun bindAppRegistry(impl: StaticAppRegistry): AppRegistry

    @Binds
    @Singleton
    abstract fun bindIntentLauncher(impl: AndroidIntentLauncher): IntentLauncher

    @Binds
    @Singleton
    abstract fun bindAppInstallationChecker(impl: AndroidAppInstallationChecker): AppInstallationChecker

    @Binds
    @Singleton
    abstract fun bindSystemServiceProvider(impl: AndroidSystemServiceProvider): SystemServiceProvider

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    @Named("basic")
    abstract fun bindBasicEngine(impl: BasicEngine): EngineInterface

    @Binds
    @Singleton
    @Named("ai")
    abstract fun bindAIEngine(impl: AIIntentEngine): EngineInterface
}
