package com.nexxlabs.chhotu.domain.registry.executables

import android.net.Uri
import com.nexxlabs.chhotu.domain.platform.IntentLauncher
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class DeepLinkExecutable(
    private val uriTemplate: String,
    private val intentLauncher: IntentLauncher
) : Executable {

    override fun execute(entities: Map<String, String>): ExecutionResult {
        var finalUri = uriTemplate
        entities.forEach { (key, value) ->
            finalUri = finalUri.replace("{$key}", Uri.encode(value))
        }
        return intentLauncher.launchUri(finalUri)
    }
}
