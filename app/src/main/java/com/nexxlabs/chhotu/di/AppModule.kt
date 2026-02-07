package com.nexxlabs.chhotu.di

import android.content.Context
import com.nexxlabs.chhotu.domain.engine.CommandNormalizer
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
 * 
 * Note: Executor bindings are in ExecutorModule using @Binds @IntoSet.
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
    fun provideGson(): com.google.gson.Gson {
        return com.google.gson.Gson()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        return okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }
}

