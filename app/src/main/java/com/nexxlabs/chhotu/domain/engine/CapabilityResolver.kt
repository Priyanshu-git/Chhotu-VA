package com.nexxlabs.chhotu.domain.engine

import android.util.Log
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.platform.AppInstallationChecker
import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.domain.registry.executables.DeepLinkExecutable
import com.nexxlabs.chhotu.domain.registry.model.Action
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityResolver @Inject constructor(
    private val appInstallationChecker: AppInstallationChecker,
    private val appRegistry: AppRegistry
) {

    fun resolveAndExecute(intent: StructuredIntent): CommandResult {
        Log.d(Constants.LOG.EXECUTOR, "Resolving intent: $intent")

        if (intent.intentType == IntentType.UNKNOWN) {
            Log.w(Constants.LOG.EXECUTOR, "Intent type is UNKNOWN")
            return CommandResult(ExecutionResult.Failure.ActionNotSupported, intent = intent)
        }

        val targetAppAlias = intent.targetApp
            ?: run {
                Log.w(Constants.LOG.EXECUTOR, "Target app alias is missing")
                return CommandResult(ExecutionResult.Failure.MissingRequiredEntities, intent = intent)
            }

        // 1. Resolve registry entry by alias
        val entry = appRegistry.findByAlias(targetAppAlias)
            ?: run {
                Log.w(Constants.LOG.EXECUTOR, "App not found in registry for alias: $targetAppAlias")
                return CommandResult(ExecutionResult.Failure.ActionNotSupported, intent = intent)
            }

        Log.d(Constants.LOG.EXECUTOR, "Found registry entry: ${entry.displayName} (${entry.packageName})")

        // 2. Check app installation (if packageName is present)
        val isInstalled = entry.packageName == null || appInstallationChecker.isInstalled(entry.packageName)
        if (!isInstalled) {
            Log.w(Constants.LOG.EXECUTOR, "App not installed: ${entry.packageName}")
        }

        // 3. Resolve action
        val actionId = intent.action ?: "OPEN"
        Log.d(Constants.LOG.EXECUTOR, "Resolving action: $actionId")

        var action = entry.actions.find {
            it.id.equals(actionId, ignoreCase = true) ||
                    it.aliases.contains(actionId.lowercase())
        }

        // Fallback to OPEN if action not found but app exists
        if (action == null) {
            Log.w(Constants.LOG.EXECUTOR, "Action '$actionId' not found for ${entry.displayName}. Falling back to OPEN.")
            action = entry.actions.find { it.id.equals("OPEN", ignoreCase = true) }
        }

        if (action == null) {
            Log.e(Constants.LOG.EXECUTOR, "No suitable action found, including OPEN")
            return CommandResult(
                ExecutionResult.Failure.ActionNotSupported,
                displayName = entry.displayName,
                intent = intent
            )
        }

        Log.d(Constants.LOG.EXECUTOR, "Selected action: ${action.id}")

        // 4. If app not installed, try deep link fallback before giving up
        if (!isInstalled) {
            val deepLink = findDeepLink(action)
            if (deepLink != null) {
                Log.d(Constants.LOG.EXECUTOR, "App not installed, using deep link fallback for ${entry.displayName}")
                val result = deepLink.execute(intent.entities)
                return CommandResult(result, displayName = entry.displayName, actionId = action.id, intent = intent)
            }
            return CommandResult(
                ExecutionResult.Failure.AppNotInstalled,
                displayName = entry.displayName,
                intent = intent
            )
        }

        // 6. Validate ActionContract
        if (!validateContract(action, intent.entities)) {
            Log.w(Constants.LOG.EXECUTOR, "Contract validation failed for ${entry.displayName} : ${action.id}. Falling back to OPEN.")
            val openAction = entry.actions.find { it.id.equals("OPEN", ignoreCase = true) }
            if (openAction != null && openAction != action) {
                Log.d(Constants.LOG.EXECUTOR, "Executing fallback OPEN action")
                val result = openAction.primaryExecutable.execute(emptyMap())
                return CommandResult(result, displayName = entry.displayName, actionId = "OPEN", intent = intent)
            }
            return CommandResult(
                ExecutionResult.Failure.MissingRequiredEntities,
                displayName = entry.displayName,
                actionId = action.id,
                intent = intent
            )
        }

        // 7. Execute primary executable
        Log.d(Constants.LOG.EXECUTOR, "Executing primary for ${entry.displayName} : ${action.id} with entities: ${intent.entities}")
        var result = action.primaryExecutable.execute(intent.entities)

        // 8. On runtime failure -> execute fallback
        if (result is ExecutionResult.Failure && action.fallbackExecutable != null) {
            Log.w(Constants.LOG.EXECUTOR, "Primary failed, executing fallback for ${entry.displayName} : ${action.id}")
            result = action.fallbackExecutable.execute(intent.entities)
        }

        Log.d(Constants.LOG.EXECUTOR, "Execution result: $result")
        return CommandResult(result, displayName = entry.displayName, actionId = action.id, intent = intent)
    }

    private fun findDeepLink(action: Action): DeepLinkExecutable? {
        return action.primaryExecutable as? DeepLinkExecutable
            ?: action.fallbackExecutable as? DeepLinkExecutable
    }

    private fun validateContract(action: Action, entities: Map<String, String>): Boolean {
        val required = action.contract.requiredEntities
        return required.all { entities.containsKey(it) }
    }
}
