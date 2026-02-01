package com.nexxlabs.chhotu.execution

/**
 * Result of executing a command intent.
 * Used to communicate the outcome back to the UI for appropriate feedback.
 */
sealed class ExecutionResult {
    
    /**
     * Command executed successfully.
     * @param feedbackMessage Message to speak via TTS
     */
    data class Success(val feedbackMessage: String) : ExecutionResult()
    
    /**
     * Target app is not installed on the device.
     * @param appName Display name of the missing app
     */
    data class AppNotInstalled(val appName: String) : ExecutionResult() {
        val feedbackMessage: String
            get() = "$appName is not installed on your device"
    }
    
    /**
     * Command execution failed.
     * @param error Description of what went wrong
     */
    data class Failure(val error: String) : ExecutionResult() {
        val feedbackMessage: String
            get() = "Sorry, something went wrong: $error"
    }
}
