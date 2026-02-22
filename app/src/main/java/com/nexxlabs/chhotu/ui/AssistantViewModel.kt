package com.nexxlabs.chhotu.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexxlabs.chhotu.data.local.CommandHistoryItem
import com.nexxlabs.chhotu.data.local.CommandHistoryRepository
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
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

/**
 * ViewModel for the assistant screen.
 * Orchestrates the command processing pipeline via CommandExecutor.
 */
@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val ttsFeedbackManager: TTSFeedbackManager,
    private val commandHistoryRepository: CommandHistoryRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val state: StateFlow<AssistantState> = _state.asStateFlow()
    
    val commandHistory: StateFlow<List<CommandHistoryItem>> =
        commandHistoryRepository.history
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
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
        addToHistory(rawText, result.executionResult, feedbackMessage)

        // Update state
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
        
        // Return to idle (only if not waiting for selection)
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
                _state.value = AssistantState.Processing(originalCommand)
                
                val updatedEntities = intent.entities.toMutableMap().apply {
                    put("contact", contact.name)
                    put("contact_number", contact.phoneNumber)
                }
                val updatedIntent = intent.copy(entities = updatedEntities)
                
                val result = commandExecutor.executeIntent(updatedIntent)
                
                val feedbackMessage = getFeedbackMessage(result)
                ttsFeedbackManager.speak(feedbackMessage)
                addToHistory(originalCommand, result.executionResult, feedbackMessage)

                if (result.executionResult is ExecutionResult.Success) {
                    _state.value = AssistantState.Success(originalCommand, feedbackMessage)
                } else {
                    _state.value = AssistantState.Error(originalCommand, feedbackMessage)
                }
                resetToIdle()
            }
        }
    }

    private fun getFeedbackMessage(result: CommandResult): String {
        val name = result.displayName
        return when (result.executionResult) {
            is ExecutionResult.Success ->
                    when (result.actionId) {
                        "OPEN" -> "Opening ${name ?: "app"}."
                        "SEARCH" -> "Searching on ${name ?: "the web"}."
                        "SEND_MESSAGE" -> "Sending message on ${name ?: "app"}."
                        "CALL" -> "Calling via ${name ?: "phone"}."
                        "INCREASE" -> "Volume increased."
                        "DECREASE" -> "Volume decreased."
                        "MUTE" -> "Volume muted."
                        "TURN_ON" -> "${name ?: "Feature"} turned on."
                        "TURN_OFF" -> "${name ?: "Feature"} turned off."
                        else -> "Done."
                    }
            is ExecutionResult.Failure.AppNotInstalled ->
                    "${name ?: "The app"} is not installed on your device."
            is ExecutionResult.Failure.ActionNotSupported ->
                    if (name != null) "I can't do that with $name." else "I can't do that yet."
            is ExecutionResult.Failure.MissingRequiredEntities ->
                    "I need more information to do that."
            is ExecutionResult.Failure.AmbiguousContact ->
                    "Multiple contacts found. Which one would you like to use?"
            is ExecutionResult.Failure.ExecutionException ->
                    "Something went wrong: ${result.executionResult.throwable.localizedMessage}"
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
