package com.nexxlabs.chhotu.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.execution.CommandExecutor
import com.nexxlabs.chhotu.speech.TTSFeedbackManager
import com.nexxlabs.chhotu.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the assistant screen.
 * Orchestrates the command processing pipeline via CommandExecutor.
 */
@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val ttsFeedbackManager: TTSFeedbackManager
) : ViewModel() {
    
    private val _state = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val state: StateFlow<AssistantState> = _state.asStateFlow()
    
    private val _commandHistory = MutableStateFlow<List<CommandHistoryItem>>(emptyList())
    val commandHistory: StateFlow<List<CommandHistoryItem>> = _commandHistory.asStateFlow()
    
    private val _typedCommand = MutableStateFlow("")
    val typedCommand: StateFlow<String> = _typedCommand.asStateFlow()

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
    
    private suspend fun processCommand(rawText: String) {
        Log.d(Constants.LOG.INPUT, "Input: $rawText")
        _state.value = AssistantState.Processing(rawText)
        
        // Execute via CommandExecutor
        val result = commandExecutor.execute(rawText)
        
        // Provide feedback
        val feedbackMessage = getFeedbackMessage(result)
        ttsFeedbackManager.speak(feedbackMessage)
        
        // Update history
        addToHistory(rawText, result, feedbackMessage)
        
        // Update state
        if (result is ExecutionResult.Success) {
            _state.value = AssistantState.Success(rawText, feedbackMessage)
        } else {
             _state.value = AssistantState.Error(rawText, feedbackMessage)
        }
        
        // Return to idle
        delay(4000)
        _state.value = AssistantState.Idle
    }
    
    private fun getFeedbackMessage(result: ExecutionResult): String {
        return when (result) {
            is ExecutionResult.Success -> "Task completed." // Generic success
            is ExecutionResult.Failure.AppNotInstalled -> "That app is not installed."
            is ExecutionResult.Failure.ActionNotSupported -> "I can't do that yet."
            is ExecutionResult.Failure.MissingRequiredEntities -> "I need more information to do that."
            is ExecutionResult.Failure.ExecutionException -> "Something went wrong: ${result.throwable.localizedMessage}"
            // Fallback for any other failure
            else -> "I couldn't understand or execute that."
        }
    }
    
    private fun addToHistory(
        originalText: String,
        result: ExecutionResult,
        feedback: String
    ) {
        val historyItem = CommandHistoryItem(
            originalText = originalText,
            intentType = "AI Command", // Simplified
            wasSuccessful = result is ExecutionResult.Success,
            feedbackMessage = feedback
        )
        
        _commandHistory.value = listOf(historyItem) + _commandHistory.value.take(9)
    }
    
    fun resetToIdle() {
        _state.value = AssistantState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
        ttsFeedbackManager.shutdown()
    }
}

/**
 * Represents a command in the history.
 */
data class CommandHistoryItem(
    val originalText: String,
    val intentType: String,
    val wasSuccessful: Boolean,
    val feedbackMessage: String
)
