package com.nexxlabs.chhotu.domain.registry.executables

import android.media.AudioManager
import com.nexxlabs.chhotu.domain.platform.SystemServiceProvider
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class VolumeExecutableTest {

    private lateinit var systemServiceProvider: SystemServiceProvider

    @Before
    fun setup() {
        systemServiceProvider = mockk()
    }

    @Test
    fun `increase volume calls adjustVolume with RAISE`() {
        every { systemServiceProvider.adjustVolume(any(), any()) } returns ExecutionResult.Success

        val executable = VolumeExecutable(VolumeExecutable.VolumeAction.INCREASE, systemServiceProvider)
        val result = executable.execute(emptyMap())

        assertEquals(ExecutionResult.Success, result)
        verify { systemServiceProvider.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI) }
    }

    @Test
    fun `decrease volume calls adjustVolume with LOWER`() {
        every { systemServiceProvider.adjustVolume(any(), any()) } returns ExecutionResult.Success

        val executable = VolumeExecutable(VolumeExecutable.VolumeAction.DECREASE, systemServiceProvider)
        val result = executable.execute(emptyMap())

        assertEquals(ExecutionResult.Success, result)
        verify { systemServiceProvider.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI) }
    }

    @Test
    fun `max volume calls setVolume with max value`() {
        every { systemServiceProvider.getMaxVolume() } returns 15
        every { systemServiceProvider.setVolume(any(), any()) } returns ExecutionResult.Success

        val executable = VolumeExecutable(VolumeExecutable.VolumeAction.MAX, systemServiceProvider)
        val result = executable.execute(emptyMap())

        assertEquals(ExecutionResult.Success, result)
        verify { systemServiceProvider.setVolume(15, AudioManager.FLAG_SHOW_UI) }
    }
}
