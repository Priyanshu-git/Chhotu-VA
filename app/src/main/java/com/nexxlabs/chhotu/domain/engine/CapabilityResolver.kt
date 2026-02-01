package com.nexxlabs.chhotu.domain.engine

import android.content.Context
import android.content.pm.PackageManager
import com.nexxlabs.chhotu.domain.model.SupportedApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves whether the device has the capability to execute commands for specific apps.
 * 
 * Primary responsibility: Check if target apps are installed before attempting
 * to launch intents, allowing for graceful user feedback when apps are missing.
 */
@Singleton
class CapabilityResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val packageManager: PackageManager = context.packageManager
    
    /**
     * Check if a specific app is installed on the device.
     * 
     * @param app The target app to check
     * @return true if the app is installed and can be launched
     */
    fun isAppInstalled(app: SupportedApp): Boolean {
        return try {
            packageManager.getPackageInfo(app.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Check if an app with the given package name is installed.
     * 
     * @param packageName The package name to check
     * @return true if the app is installed
     */
    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Get a list of all supported apps that are currently installed.
     * 
     * @return List of installed supported apps
     */
    fun getInstalledSupportedApps(): List<SupportedApp> {
        return SupportedApp.entries.filter { isAppInstalled(it) }
    }
}
