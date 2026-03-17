package com.nexxlabs.chhotu.data.local

import com.nexxlabs.chhotu.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val appDataStore: AppDataStore
) {
    val themeMode: Flow<ThemeMode> = appDataStore
        .getStringFlow(AppDataStore.THEME_MODE, ThemeMode.SYSTEM.name)
        .map { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM) }

    suspend fun setThemeMode(mode: ThemeMode) {
        appDataStore.putString(AppDataStore.THEME_MODE, mode.name)
    }

    val hasCompletedOnboarding: Flow<Boolean> = appDataStore
        .getStringFlow(AppDataStore.ONBOARDING_COMPLETED, "false")
        .map { it == "true" }

    suspend fun setOnboardingCompleted() {
        appDataStore.putString(AppDataStore.ONBOARDING_COMPLETED, "true")
    }

    val speechLanguage: Flow<String> = appDataStore
        .getStringFlow(AppDataStore.SPEECH_LANGUAGE, "System default")

    suspend fun setSpeechLanguage(language: String) {
        appDataStore.putString(AppDataStore.SPEECH_LANGUAGE, language)
    }
}
