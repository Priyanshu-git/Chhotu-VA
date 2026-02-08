package com.nexxlabs.chhotu.domain.registry.executables

import android.content.Context
import android.media.AudioManager
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class VolumeExecutable(private val actionType: VolumeAction) : Executable {

    enum class VolumeAction {
        INCREASE, DECREASE, MUTE, UNMUTE, MAX, MIN
    }

    override fun execute(context: Context, entities: Map<String, String>): ExecutionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val streamType = AudioManager.STREAM_MUSIC // Default to music stream

        try {
            when (actionType) {
                VolumeAction.INCREASE -> {
                    audioManager.adjustStreamVolume(
                        streamType, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
                    )
                }

                VolumeAction.DECREASE -> {
                    audioManager.adjustStreamVolume(
                        streamType, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
                    )
                }

                VolumeAction.MUTE -> {
                    audioManager.adjustStreamVolume(
                        streamType, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI
                    )
                }

                VolumeAction.UNMUTE -> {
                    audioManager.adjustStreamVolume(
                        streamType, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI
                    )
                }

                VolumeAction.MAX -> {
                    val max = audioManager.getStreamMaxVolume(streamType)
                    audioManager.setStreamVolume(streamType, max, AudioManager.FLAG_SHOW_UI)
                }

                VolumeAction.MIN -> {
                    val min = audioManager.getStreamMinVolume(streamType) // API 28+ for getMin
                    audioManager.setStreamVolume(streamType, min, AudioManager.FLAG_SHOW_UI)
                }
            }
            return ExecutionResult.Success
        } catch (e: Exception) {
            return ExecutionResult.Failure.ExecutionException(e)
        }
    }
}
