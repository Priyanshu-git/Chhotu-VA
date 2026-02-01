package com.nexxlabs.chhotu.execution

import com.nexxlabs.chhotu.domain.model.CommandIntent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central command executor that routes CommandIntent to appropriate executors.
 * Acts as a facade over the individual executors.
 */
@Singleton
class CommandExecutor @Inject constructor(
    private val googleSearchExecutor: GoogleSearchExecutor,
    private val youtubeMusicExecutor: YouTubeMusicExecutor,
    private val whatsAppExecutor: WhatsAppExecutor,
    private val appLauncher: AppLauncher
) {
    
    /**
     * Execute a command intent using the appropriate executor.
     * 
     * @param intent The command to execute
     * @return ExecutionResult with feedback message
     */
    fun execute(intent: CommandIntent): ExecutionResult {
        return when (intent) {
            is CommandIntent.Search -> {
                googleSearchExecutor.execute(intent.query)
            }
            is CommandIntent.PlayMusic -> {
                youtubeMusicExecutor.execute(intent.query)
            }
            is CommandIntent.SendWhatsAppMessage -> {
                whatsAppExecutor.execute(intent.message)
            }
            is CommandIntent.OpenApp -> {
                appLauncher.launch(intent.app)
            }
            is CommandIntent.Unknown -> {
                ExecutionResult.Failure(intent.feedbackMessage)
            }
        }
    }
}
