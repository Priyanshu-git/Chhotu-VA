package com.nexxlabs.chhotu.domain.registry.executables

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class IntentExecutable(
    private val action: String,
    private val packageName: String?
) : Executable {

    override fun execute(context: Context, entities: Map<String, String>): ExecutionResult {
        return try {
            val intent = when {
                action == Intent.ACTION_MAIN && packageName != null -> {
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?: throw ActivityNotFoundException("No launch intent for $packageName")
                }
                else -> {
                    Intent(action).apply {
                        packageName?.let { setPackage(it) }
                    }
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            entities.forEach { (key, value) ->
                intent.putExtra(key, value)
            }

            context.startActivity(intent)
            ExecutionResult.Success
        } catch (e: Exception) {
            ExecutionResult.Failure.ExecutionException(e)
        }
    }
}
