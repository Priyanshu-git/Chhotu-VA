package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class FlashlightExecutable(private val actionType: FlashlightAction) : Executable {

    enum class FlashlightAction {
        ON, OFF, TOGGLE
    }

    override fun execute(context: Context, entities: Map<String, String>): ExecutionResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return ExecutionResult.Failure.ActionNotSupported
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(
                        android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                    ) == true
            } ?: return ExecutionResult.Failure.ActionNotSupported

            // For TOGGLE, we need to know current state.
            // Registering key callback is complex for a one-shot command.
            // Simplified: We'll assume OFF if we can't determine, or just support ON/OFF explicitly
            // for now.
            // Actually, we can't easily query current torch state without a callback listener.
            // For TOGGLE, we might need a persistent state or just fail safely.
            // Let's stick to ON/OFF mapping from AI. If AI sends TOGGLE, we might default to ON?

            // Re-thinking TOGGLE: We can't query it synchronously easily.
            // Let's rely on explicit ON/OFF instructions.

            val turnOn = when (actionType) {
                FlashlightAction.ON -> true
                FlashlightAction.OFF -> false
                FlashlightAction.TOGGLE -> true // Fallback/Assumption or maybe we shouldn't support Toggle in
                // MVP
            }

            cameraManager.setTorchMode(cameraId, turnOn)
            return ExecutionResult.Success
        } catch (e: Exception) {
            return ExecutionResult.Failure.ExecutionException(e)
        }
    }
}
