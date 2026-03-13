package com.nexxlabs.chhotu.domain.registry.executables

import android.media.AudioManager
import com.nexxlabs.chhotu.domain.platform.SystemServiceProvider
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class VolumeExecutable(
    private val actionType: VolumeAction,
    private val systemServiceProvider: SystemServiceProvider
) : Executable {

    enum class VolumeAction {
        INCREASE, DECREASE, MUTE, UNMUTE, MAX, MIN
    }

    override fun execute(entities: Map<String, String>): ExecutionResult {
        return when (actionType) {
            VolumeAction.INCREASE -> systemServiceProvider.adjustVolume(
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
            )
            VolumeAction.DECREASE -> systemServiceProvider.adjustVolume(
                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
            )
            VolumeAction.MUTE -> systemServiceProvider.adjustVolume(
                AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI
            )
            VolumeAction.UNMUTE -> systemServiceProvider.adjustVolume(
                AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI
            )
            VolumeAction.MAX -> systemServiceProvider.setVolume(
                systemServiceProvider.getMaxVolume(), AudioManager.FLAG_SHOW_UI
            )
            VolumeAction.MIN -> systemServiceProvider.setVolume(
                systemServiceProvider.getMinVolume(), AudioManager.FLAG_SHOW_UI
            )
        }
    }
}
