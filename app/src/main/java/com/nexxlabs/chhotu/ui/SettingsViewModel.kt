package com.nexxlabs.chhotu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexxlabs.chhotu.data.local.CommandHistoryRepository
import com.nexxlabs.chhotu.data.local.SettingsRepository
import com.nexxlabs.chhotu.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val commandHistoryRepository: CommandHistoryRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val speechLanguage: StateFlow<String> = settingsRepository.speechLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System default")

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setSpeechLanguage(language: String) {
        viewModelScope.launch { settingsRepository.setSpeechLanguage(language) }
    }

    fun clearHistory() {
        viewModelScope.launch { commandHistoryRepository.clear() }
    }
}
