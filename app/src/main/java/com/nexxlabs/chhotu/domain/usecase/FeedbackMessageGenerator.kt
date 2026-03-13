package com.nexxlabs.chhotu.domain.usecase

import com.nexxlabs.chhotu.domain.constants.ActionIds
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackMessageGenerator @Inject constructor() {

    fun generate(result: CommandResult): String {
        val name = result.displayName
        return when (result.executionResult) {
            is ExecutionResult.Success -> when (result.actionId) {
                ActionIds.OPEN -> "Opening ${name ?: "app"}."
                ActionIds.SEARCH -> "Searching on ${name ?: "the web"}."
                ActionIds.SEND_MESSAGE -> "Sending message on ${name ?: "app"}."
                ActionIds.CALL -> "Calling via ${name ?: "phone"}."
                ActionIds.INCREASE -> "Volume increased."
                ActionIds.DECREASE -> "Volume decreased."
                ActionIds.MUTE -> "Volume muted."
                ActionIds.TURN_ON -> "${name ?: "Feature"} turned on."
                ActionIds.TURN_OFF -> "${name ?: "Feature"} turned off."
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
}
