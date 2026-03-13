package com.nexxlabs.chhotu.data.platform

import android.content.Context
import android.content.pm.PackageManager
import com.nexxlabs.chhotu.domain.platform.AppInstallationChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAppInstallationChecker @Inject constructor(
    @ApplicationContext private val context: Context
) : AppInstallationChecker {

    override fun isInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
