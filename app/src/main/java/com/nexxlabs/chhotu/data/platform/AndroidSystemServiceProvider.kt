package com.nexxlabs.chhotu.data.platform

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import com.nexxlabs.chhotu.domain.platform.SystemServiceProvider
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSystemServiceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : SystemServiceProvider {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun adjustVolume(direction: Int, flags: Int): ExecutionResult {
        return try {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, flags)
            ExecutionResult.Success
        } catch (e: Exception) {
            ExecutionResult.Failure.ExecutionException(e)
        }
    }

    override fun setVolume(volume: Int, flags: Int): ExecutionResult {
        return try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, flags)
            ExecutionResult.Success
        } catch (e: Exception) {
            ExecutionResult.Failure.ExecutionException(e)
        }
    }

    override fun getMaxVolume(): Int =
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    override fun getMinVolume(): Int =
        audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)

    override fun setTorchMode(enabled: Boolean): ExecutionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return ExecutionResult.Failure.ActionNotSupported

            cameraManager.setTorchMode(cameraId, enabled)
            ExecutionResult.Success
        } catch (e: Exception) {
            ExecutionResult.Failure.ExecutionException(e)
        }
    }
}
