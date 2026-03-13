package com.nexxlabs.chhotu.domain.platform

import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

interface IntentLauncher {
    fun launchApp(packageName: String): ExecutionResult
    fun launchIntent(action: String, packageName: String? = null, extras: Map<String, String> = emptyMap()): ExecutionResult
    fun launchUri(uriString: String): ExecutionResult
    fun launchUriWithExtras(action: String, uriString: String, extras: Map<String, String> = emptyMap()): ExecutionResult
}
