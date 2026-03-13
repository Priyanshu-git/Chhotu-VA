package com.nexxlabs.chhotu.domain.registry.executables

import com.nexxlabs.chhotu.domain.platform.SystemServiceProvider
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

class FlashlightExecutable(
    private val actionType: FlashlightAction,
    private val systemServiceProvider: SystemServiceProvider
) : Executable {

    enum class FlashlightAction {
        ON, OFF, TOGGLE
    }

    override fun execute(entities: Map<String, String>): ExecutionResult {
        val turnOn = when (actionType) {
            FlashlightAction.ON -> true
            FlashlightAction.OFF -> false
            FlashlightAction.TOGGLE -> true // Can't query state synchronously; default to ON
        }
        return systemServiceProvider.setTorchMode(turnOn)
    }
}
