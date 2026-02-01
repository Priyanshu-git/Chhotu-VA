package com.nexxlabs.chhotu.execution

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.nexxlabs.chhotu.domain.engine.CapabilityResolver
import com.nexxlabs.chhotu.domain.model.CommandIntent
import com.nexxlabs.chhotu.domain.model.SupportedApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Launches supported apps using the system package manager.
 * Handles cases where apps are not installed gracefully.
 */
@Singleton
class AppLaunchExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val capabilityResolver: CapabilityResolver
) : AppExecutor {
    
    override val supportedIntentTypes: Set<KClass<out CommandIntent>> = setOf(
        CommandIntent.OpenApp::class
    )
    
    override fun execute(intent: CommandIntent): ExecutionResult {
        val openAppIntent = intent as? CommandIntent.OpenApp
            ?: return ExecutionResult.Failure("Invalid intent type for AppLaunchExecutor")
        return executeLaunch(openAppIntent.app)
    }
    
    /**
     * Launch a supported app.
     * 
     * @param app The app to launch
     * @return ExecutionResult indicating success or failure
     */
    private fun executeLaunch(app: SupportedApp): ExecutionResult {
        if (!capabilityResolver.isAppInstalled(app)) {
            return ExecutionResult.AppNotInstalled(app.displayName)
        }
        
        return try {
            val launchIntent = context.packageManager
                .getLaunchIntentForPackage(app.packageName)
            
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ExecutionResult.Success("Opening ${app.displayName}")
            } else {
                ExecutionResult.Failure("Could not find ${app.displayName}")
            }
        } catch (e: ActivityNotFoundException) {
            ExecutionResult.AppNotInstalled(app.displayName)
        } catch (e: Exception) {
            ExecutionResult.Failure(e.message ?: "Unknown error")
        }
    }
}
