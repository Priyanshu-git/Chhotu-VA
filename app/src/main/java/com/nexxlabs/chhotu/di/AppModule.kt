package com.nexxlabs.chhotu.di

import android.content.Context
import com.nexxlabs.chhotu.domain.engine.CapabilityResolver
import com.nexxlabs.chhotu.domain.engine.CommandNormalizer
import com.nexxlabs.chhotu.domain.engine.RuleBasedDecisionEngine
import com.nexxlabs.chhotu.execution.AppLauncher
import com.nexxlabs.chhotu.execution.CommandExecutor
import com.nexxlabs.chhotu.execution.GoogleSearchExecutor
import com.nexxlabs.chhotu.execution.WhatsAppExecutor
import com.nexxlabs.chhotu.execution.YouTubeMusicExecutor
import com.nexxlabs.chhotu.speech.SpeechInputManager
import com.nexxlabs.chhotu.speech.TTSFeedbackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing app-wide dependencies.
 * All dependencies are scoped to SingletonComponent for app lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideCommandNormalizer(): CommandNormalizer {
        return CommandNormalizer()
    }
    
    @Provides
    @Singleton
    fun provideRuleBasedDecisionEngine(): RuleBasedDecisionEngine {
        return RuleBasedDecisionEngine()
    }
    
    @Provides
    @Singleton
    fun provideCapabilityResolver(
        @ApplicationContext context: Context
    ): CapabilityResolver {
        return CapabilityResolver(context)
    }
    
    @Provides
    @Singleton
    fun provideSpeechInputManager(
        @ApplicationContext context: Context
    ): SpeechInputManager {
        return SpeechInputManager(context)
    }
    
    @Provides
    @Singleton
    fun provideTTSFeedbackManager(
        @ApplicationContext context: Context
    ): TTSFeedbackManager {
        return TTSFeedbackManager(context)
    }
    
    @Provides
    @Singleton
    fun provideGoogleSearchExecutor(
        @ApplicationContext context: Context,
        capabilityResolver: CapabilityResolver
    ): GoogleSearchExecutor {
        return GoogleSearchExecutor(context, capabilityResolver)
    }
    
    @Provides
    @Singleton
    fun provideYouTubeMusicExecutor(
        @ApplicationContext context: Context,
        capabilityResolver: CapabilityResolver
    ): YouTubeMusicExecutor {
        return YouTubeMusicExecutor(context, capabilityResolver)
    }
    
    @Provides
    @Singleton
    fun provideWhatsAppExecutor(
        @ApplicationContext context: Context,
        capabilityResolver: CapabilityResolver
    ): WhatsAppExecutor {
        return WhatsAppExecutor(context, capabilityResolver)
    }
    
    @Provides
    @Singleton
    fun provideAppLauncher(
        @ApplicationContext context: Context,
        capabilityResolver: CapabilityResolver
    ): AppLauncher {
        return AppLauncher(context, capabilityResolver)
    }
    
    @Provides
    @Singleton
    fun provideCommandExecutor(
        googleSearchExecutor: GoogleSearchExecutor,
        youtubeMusicExecutor: YouTubeMusicExecutor,
        whatsAppExecutor: WhatsAppExecutor,
        appLauncher: AppLauncher
    ): CommandExecutor {
        return CommandExecutor(
            googleSearchExecutor,
            youtubeMusicExecutor,
            whatsAppExecutor,
            appLauncher
        )
    }
}
