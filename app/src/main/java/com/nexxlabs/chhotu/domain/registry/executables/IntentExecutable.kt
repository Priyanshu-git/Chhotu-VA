package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Intent
import com.nexxlabs.chhotu.domain.platform.IntentLauncher
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class IntentExecutable(
    private val action: String,
    private val packageName: String?,
    private val intentLauncher: IntentLauncher
) : Executable {

    override fun execute(entities: Map<String, String>): ExecutionResult {
        return if (action == Intent.ACTION_MAIN && packageName != null) {
            intentLauncher.launchApp(packageName)
        } else {
            intentLauncher.launchIntent(action, packageName, entities)
        }
    }
}
