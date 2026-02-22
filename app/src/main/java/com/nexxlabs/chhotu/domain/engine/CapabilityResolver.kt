package com.nexxlabs.chhotu.domain.engine

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.domain.registry.model.Action
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves registry entries and executes actions based on StructuredIntent.
 * Replaces the old CapabilityResolver logic with Registry-based logic.
 */
@Singleton
class CapabilityResolver
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val appRegistry: AppRegistry
) {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Resolve and execute the intent.
     */
    fun resolveAndExecute(intent: StructuredIntent): CommandResult {
        Log.d(Constants.LOG.EXECUTOR, "Resolving intent: $intent")

        if (intent.intentType == IntentType.UNKNOWN) {
            Log.w(Constants.LOG.EXECUTOR, "Intent type is UNKNOWN")
            return CommandResult(ExecutionResult.Failure.ActionNotSupported, intent = intent)
        }

        val targetAppAlias =
            intent.targetApp
                ?: run {
                    Log.w(Constants.LOG.EXECUTOR, "Target app alias is missing")
                    return CommandResult(ExecutionResult.Failure.MissingRequiredEntities, intent = intent)
                }

        // 1. Resolve registry entry by alias
        val entry =
            appRegistry.findByAlias(targetAppAlias)
                ?: run {
                    Log.w(Constants.LOG.EXECUTOR, "App not found in registry for alias: $targetAppAlias")
                    return CommandResult(ExecutionResult.Failure.ActionNotSupported, intent = intent)
                }

        Log.d(Constants.LOG.EXECUTOR, "Found registry entry: ${entry.displayName} (${entry.packageName})")

        // 2. Check app installation (if packageName is present)
        if (entry.packageName != null && !isPackageInstalled(entry.packageName)) {
            Log.w(Constants.LOG.EXECUTOR, "App not installed: ${entry.packageName}")
            return CommandResult(
                ExecutionResult.Failure.AppNotInstalled,
                displayName = entry.displayName,
                intent = intent
            )
        }

        // 3. Resolve action
        val actionId = intent.action ?: "OPEN"
        Log.d(Constants.LOG.EXECUTOR, "Resolving action: $actionId")

        // Try to find action by ID or Alias
        var action =
            entry.actions.find {
                it.id.equals(actionId, ignoreCase = true) ||
                        it.aliases.contains(actionId.lowercase())
            }

        // Fallback to OPEN if action not found but app exists
        if (action == null) {
            Log.w(
                Constants.LOG.EXECUTOR,
                "Action '$actionId' not found for ${entry.displayName}. Falling back to OPEN."
            )
            action = entry.actions.find { it.id.equals("OPEN", ignoreCase = true) }
        }

        if (action == null) {
            Log.e(Constants.LOG.EXECUTOR, "No suitable action found, including OPEN")
            return CommandResult(
                ExecutionResult.Failure.ActionNotSupported, // Even OPEN action not found
                displayName = entry.displayName,
                intent = intent
            )
        }

        Log.d(Constants.LOG.EXECUTOR, "Selected action: ${action.id}")

        // 4. Validate ActionContract
        if (!validateContract(action, intent.entities)) {
            // specific to user request: "if we are able to find the app, but action is not
            // supported (or contract fails), we should open the app"
            Log.w(
                Constants.LOG.EXECUTOR,
                "Contract validation failed for ${entry.displayName} : ${action.id}. Falling back to OPEN."
            )

            val openAction = entry.actions.find { it.id.equals("OPEN", ignoreCase = true) }
            if (openAction != null && openAction != action) {
                Log.d(Constants.LOG.EXECUTOR, "Executing fallback OPEN action")
                val result = openAction.primaryExecutable.execute(context, emptyMap())
                return CommandResult(result, displayName = entry.displayName, actionId = "OPEN", intent = intent)
            }
            return CommandResult(
                ExecutionResult.Failure.MissingRequiredEntities,
                displayName = entry.displayName,
                actionId = action.id,
                intent = intent
            )
        }

        // 5. Execute primary executable
        Log.d(Constants.LOG.EXECUTOR,
            "Executing primary for ${entry.displayName} : ${action.id} with entities: ${intent.entities}")
        var result = action.primaryExecutable.execute(context, intent.entities)

        // 6. On runtime failure -> execute fallback
        if (result is ExecutionResult.Failure && action.fallbackExecutable != null) {
            Log.w(
                Constants.LOG.EXECUTOR,
                "Primary failed, executing fallback for ${entry.displayName} : ${action.id}"
            )
            result = action.fallbackExecutable.execute(context, intent.entities)
        }

        Log.d(Constants.LOG.EXECUTOR, "Execution result: $result")
        return CommandResult(result, displayName = entry.displayName, actionId = action.id, intent = intent)
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun validateContract(action: Action, entities: Map<String, String>): Boolean {
        val required = action.contract.requiredEntities
        // Check if all required entities are present in the provided entities map
        return required.all { entities.containsKey(it) }
    }
}
