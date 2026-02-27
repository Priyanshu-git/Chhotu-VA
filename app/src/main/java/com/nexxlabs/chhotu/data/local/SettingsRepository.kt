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
}
