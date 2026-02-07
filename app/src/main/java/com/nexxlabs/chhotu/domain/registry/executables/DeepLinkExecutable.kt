package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class DeepLinkExecutable(
    private val uriTemplate: String // e.g., "https://www.google.com/search?q={query}"
) : Executable {
    override fun execute(context: Context, entities: Map<String, String>): ExecutionResult {
        return try {
            var finalUri = uriTemplate
            entities.forEach { (key, value) -> 
                finalUri = finalUri.replace("{$key}", Uri.encode(value))
            }
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUri)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ExecutionResult.Success
        } catch (e: Exception) {
            ExecutionResult.Failure.ExecutionException(e)
        }
    }
}
