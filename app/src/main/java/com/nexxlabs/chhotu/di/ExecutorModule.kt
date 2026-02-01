package com.nexxlabs.chhotu.di

import com.nexxlabs.chhotu.execution.AppExecutor
import com.nexxlabs.chhotu.execution.AppLaunchExecutor
import com.nexxlabs.chhotu.execution.GoogleSearchExecutor
import com.nexxlabs.chhotu.execution.WhatsAppExecutor
import com.nexxlabs.chhotu.execution.YouTubeMusicExecutor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Hilt module for binding app executors into the executor registry.
 * Each executor is bound into a Set<AppExecutor> using @IntoSet.
 * 
 * To add a new executor:
 * 1. Create the executor class implementing AppExecutor
 * 2. Add a @Binds @IntoSet method here
 * 
 * That's it - the registry will automatically pick it up!
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ExecutorModule {
    
    @Binds
    @IntoSet
    abstract fun bindGoogleSearchExecutor(impl: GoogleSearchExecutor): AppExecutor
    
    @Binds
    @IntoSet
    abstract fun bindYouTubeMusicExecutor(impl: YouTubeMusicExecutor): AppExecutor
    
    @Binds
    @IntoSet
    abstract fun bindWhatsAppExecutor(impl: WhatsAppExecutor): AppExecutor
    
    @Binds
    @IntoSet
    abstract fun bindAppLaunchExecutor(impl: AppLaunchExecutor): AppExecutor
}
