package com.nexxlabs.chhotu.domain.platform

interface AppInstallationChecker {
    fun isInstalled(packageName: String): Boolean
}
