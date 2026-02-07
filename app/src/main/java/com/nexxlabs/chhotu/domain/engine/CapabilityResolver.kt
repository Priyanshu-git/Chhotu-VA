package com.nexxlabs.chhotu.domain.engine

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.domain.registry.model.Action
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
class CapabilityResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appRegistry: AppRegistry
) {
    
    private val packageManager: PackageManager = context.packageManager

    /**
     * Resolve and execute the intent.
     */
    fun resolveAndExecute(intent: StructuredIntent): ExecutionResult {
        if (intent.intentType == IntentType.UNKNOWN) {
            return ExecutionResult.Failure.ActionNotSupported // Or unknown
        }

        val targetAppAlias = intent.targetApp ?: return ExecutionResult.Failure.MissingRequiredEntities
        
        // 1. Resolve registry entry by alias
        val entry = appRegistry.findByAlias(targetAppAlias) 
            ?: return ExecutionResult.Failure.ActionNotSupported // App not found in registry

        // 2. Check app installation (if packageName is present)
        if (entry.packageName != null && !isPackageInstalled(entry.packageName)) {
            return ExecutionResult.Failure.AppNotInstalled
        }

        // 3. Resolve action
        val actionId = intent.action ?: "OPEN"

        // Try to find action by ID or Alias
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
             return ExecutionResult.Failure.ActionNotSupported // Even OPEN action not found
        }

        // 4. Validate ActionContract
        if (!validateContract(action, intent.entities)) {
            // specific to user request: "if we are able to find the app, but action is not supported (or contract fails), we should open the app"
             Log.w(Constants.LOG.EXECUTOR, "Contract validation failed for ${entry.displayName} : ${action.id}. Falling back to OPEN.")
             val openAction = entry.actions.find { it.id.equals("OPEN", ignoreCase = true) }
             if (openAction != null && openAction != action) {
                 return openAction.primaryExecutable.execute(context, emptyMap())
             }
             return ExecutionResult.Failure.MissingRequiredEntities
        }

        // 5. Execute primary executable
        Log.d(Constants.LOG.EXECUTOR, "Executing primary for ${entry.displayName} : ${action.id}")
        val result = action.primaryExecutable.execute(context, intent.entities)

        // 6. On runtime failure -> execute fallback
        if (result is ExecutionResult.Failure && action.fallbackExecutable != null) {
            Log.w(Constants.LOG.EXECUTOR, "Primary failed, executing fallback for ${entry.displayName} : ${action.id}")
            return action.fallbackExecutable.execute(context, intent.entities)
        }

        return result
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
