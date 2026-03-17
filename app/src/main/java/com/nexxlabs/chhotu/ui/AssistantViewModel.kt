package com.nexxlabs.chhotu.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexxlabs.chhotu.data.local.CommandHistoryItem
import com.nexxlabs.chhotu.data.local.CommandHistoryRepository
import com.nexxlabs.chhotu.data.local.SettingsRepository
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.domain.usecase.FeedbackMessageGenerator
import com.nexxlabs.chhotu.execution.CommandExecutor
import com.nexxlabs.chhotu.speech.TTSFeedbackManager
import com.nexxlabs.chhotu.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val ttsFeedbackManager: TTSFeedbackManager,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val feedbackMessageGenerator: FeedbackMessageGenerator,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val state: StateFlow<AssistantState> = _state.asStateFlow()

    val commandHistory: StateFlow<List<CommandHistoryItem>> =
        commandHistoryRepository.history
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _typedCommand = MutableStateFlow("")
    val typedCommand: StateFlow<String> = _typedCommand.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.hasCompletedOnboarding.collect { completed ->
                _showOnboarding.value = !completed
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
            _showOnboarding.value = false
        }
    }

    fun onTypedCommandChange(text: String) {
        _typedCommand.value = text
    }

    fun onTypedCommandSubmit() {
        val command = _typedCommand.value
        if (command.isNotBlank()) {
            viewModelScope.launch {
                _typedCommand.value = ""
                processCommand(command)
            }
        }
    }

    fun onStartListening() {
        _state.value = AssistantState.Listening
    }

    fun onSpeechRecognized(text: String) {
        viewModelScope.launch {
            processCommand(text)
        }
    }

    fun onSpeechError(errorMessage: String) {
        _state.value = AssistantState.Error(null, errorMessage)
        ttsFeedbackManager.speak(errorMessage)

        viewModelScope.launch {
            delay(3000)
            _state.value = AssistantState.Idle
        }
    }

    fun deleteHistoryItem(item: CommandHistoryItem) {
        viewModelScope.launch {
            commandHistoryRepository.removeItem(item)
        }
    }

    private suspend fun processCommand(rawText: String) {
        Log.d(Constants.LOG.INPUT, "Input: $rawText")
        _state.value = AssistantState.Processing(rawText)

        val result = commandExecutor.execute(rawText)
        val feedbackMessage = feedbackMessageGenerator.generate(result)
        ttsFeedbackManager.speak(feedbackMessage)
        addToHistory(rawText, result.executionResult, feedbackMessage)

        when (result.executionResult) {
            is ExecutionResult.Success -> {
                _state.value = AssistantState.Success(rawText, feedbackMessage)
            }
            is ExecutionResult.Failure.AmbiguousContact -> {
                _state.value = AssistantState.SelectContact(
                    result.executionResult.contacts,
                    rawText,
                    result.intent ?: throw IllegalStateException("Intent missing in AmbiguousContact result")
                )
            }
            else -> {
                _state.value = AssistantState.Error(rawText, feedbackMessage)
            }
        }

        if (result.executionResult !is ExecutionResult.Failure.AmbiguousContact) {
            resetToIdle()
        }
    }

    fun onContactSelected(contact: com.nexxlabs.chhotu.domain.model.Contact) {
        val currentState = _state.value
        if (currentState is AssistantState.SelectContact) {
            val originalCommand = currentState.originalCommand
            val intent = currentState.intent

            viewModelScope.launch {
                try {
                    _state.value = AssistantState.Processing(originalCommand)

                    val updatedEntities = intent.entities.toMutableMap().apply {
                        put("contact", contact.name)
                        put("contact_number", contact.phoneNumber)
                    }
                    val updatedIntent = intent.copy(entities = updatedEntities)

                    val result = commandExecutor.executeIntent(updatedIntent)
                    val feedbackMessage = feedbackMessageGenerator.generate(result)
                    ttsFeedbackManager.speak(feedbackMessage)
                    addToHistory(originalCommand, result.executionResult, feedbackMessage)

                    if (result.executionResult is ExecutionResult.Success) {
                        _state.value = AssistantState.Success(originalCommand, feedbackMessage)
                    } else {
                        _state.value = AssistantState.Error(originalCommand, feedbackMessage)
                    }
                    resetToIdle()
                } catch (e: Exception) {
                    Log.e(Constants.LOG.EXECUTOR, "Contact selection failed", e)
                    _state.value = AssistantState.Error(originalCommand, "Something went wrong.")
                    resetToIdle()
                }
            }
        }
    }

    private fun addToHistory(
        originalText: String,
        result: ExecutionResult,
        feedback: String
    ) {
        val historyItem = CommandHistoryItem(
            originalText = originalText,
            intentType = "AI Command",
            wasSuccessful = result is ExecutionResult.Success || result is ExecutionResult.Failure.AmbiguousContact,
            feedbackMessage = feedback
        )

        viewModelScope.launch {
            commandHistoryRepository.addItem(historyItem)
        }
    }

    fun resetToIdle() {
        _state.value = AssistantState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        ttsFeedbackManager.shutdown()
    }
}
