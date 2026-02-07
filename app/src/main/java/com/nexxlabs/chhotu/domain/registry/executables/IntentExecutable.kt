package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Context
import android.content.Intent
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class IntentExecutable(
    private val action: String,
    private val packageName: String
) : Executable {
    override fun execute(context: Context, entities: Map<String, String>): ExecutionResult {
        return try {
            val intent = Intent(action).apply {
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Add all entities as extras
                entities.forEach { (key, value) -> putExtra(key, value) }
            }
            context.startActivity(intent)
            ExecutionResult.Success
        } catch (e: Exception) {
            ExecutionResult.Failure.ExecutionException(e)
        }
    }
}
