package com.nexxlabs.chhotu.domain.platform

import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult

interface SystemServiceProvider {
    fun adjustVolume(direction: Int, flags: Int): ExecutionResult
    fun setVolume(volume: Int, flags: Int): ExecutionResult
    fun getMaxVolume(): Int
    fun getMinVolume(): Int
    fun setTorchMode(enabled: Boolean): ExecutionResult
}
