package com.nexxlabs.chhotu.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexxlabs.chhotu.domain.engine.CommandNormalizer
import com.nexxlabs.chhotu.domain.engine.RuleBasedDecisionEngine
import com.nexxlabs.chhotu.domain.model.CommandIntent
import com.nexxlabs.chhotu.execution.CommandExecutor
import com.nexxlabs.chhotu.execution.ExecutionResult
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
 * Orchestrates the command processing pipeline:
 * Speech Input → Normalize → Decide → Execute → Feedback
 */
@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val commandNormalizer: CommandNormalizer,
    private val decisionEngine: RuleBasedDecisionEngine,
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

    /**
     * Called when entering listening state.
     */
    fun onStartListening() {
        _state.value = AssistantState.Listening
    }
    
    /**
     * Called when speech recognition completes successfully.
     * Processes the recognized text through the command pipeline.
     */
    fun onSpeechRecognized(text: String) {
        viewModelScope.launch {
            processCommand(text)
        }
    }
    
    /**
     * Called when speech recognition fails or is cancelled.
     */
    fun onSpeechError(errorMessage: String) {
        _state.value = AssistantState.Error(null, errorMessage)
        ttsFeedbackManager.speak(errorMessage)
        
        // Return to idle after delay
        viewModelScope.launch {
            delay(3000)
            _state.value = AssistantState.Idle
        }
    }
    
    /**
     * Process a voice command through the full pipeline.
     */
    private suspend fun processCommand(rawText: String) {
        Log.d(Constants.LOG.INPUT, "Input: $rawText")
        // 1. Update state to processing
        _state.value = AssistantState.Processing(rawText)
        
        // 2. Normalize the input
        val normalizedText = commandNormalizer.normalize(rawText)
        Log.d(Constants.LOG.INPUT, "Normalized: $normalizedText")
        
        // 3. Decide what action to take
        val commandIntent = decisionEngine.decide(normalizedText)
        
        // 4. Execute the command
        val result = commandExecutor.execute(commandIntent)
        
        // 5. Provide feedback and update state
        val feedbackMessage = when (result) {
            is ExecutionResult.Success -> result.feedbackMessage
            is ExecutionResult.AppNotInstalled -> result.feedbackMessage
            is ExecutionResult.Failure -> result.feedbackMessage
        }
        
        ttsFeedbackManager.speak(feedbackMessage)
        
        // 6. Update history
        addToHistory(rawText, commandIntent, result)
        
        // 7. Update final state
        when (result) {
            is ExecutionResult.Success -> {
                _state.value = AssistantState.Success(rawText, feedbackMessage)
            }
            is ExecutionResult.AppNotInstalled,
            is ExecutionResult.Failure -> {
                _state.value = AssistantState.Error(rawText, feedbackMessage)
            }
        }
        
        // 8. Return to idle after delay
        delay(4000)
        _state.value = AssistantState.Idle
    }
    
    private fun addToHistory(
        originalText: String,
        intent: CommandIntent,
        result: ExecutionResult
    ) {
        val historyItem = CommandHistoryItem(
            originalText = originalText,
            intentType = intent::class.simpleName ?: "Unknown",
            wasSuccessful = result is ExecutionResult.Success,
            feedbackMessage = when (result) {
                is ExecutionResult.Success -> result.feedbackMessage
                is ExecutionResult.AppNotInstalled -> result.feedbackMessage
                is ExecutionResult.Failure -> result.feedbackMessage
            }
        )
        
        _commandHistory.value = listOf(historyItem) + _commandHistory.value.take(9)
    }
    
    /**
     * Reset to idle state manually.
     */
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
