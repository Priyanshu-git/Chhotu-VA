package com.nexxlabs.chhotu.ui

import android.util.Log
import app.cash.turbine.test
import com.nexxlabs.chhotu.data.local.CommandHistoryRepository
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.execution.CommandExecutor
import com.nexxlabs.chhotu.speech.TTSFeedbackManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModelTest {

    private lateinit var commandExecutor: CommandExecutor
    private lateinit var ttsFeedbackManager: TTSFeedbackManager
    private lateinit var viewModel: AssistantViewModel
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var  commandHistoryRepository:  CommandHistoryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0

        commandExecutor = mockk()
        ttsFeedbackManager = mockk(relaxed = true)
        commandHistoryRepository = mockk()

        viewModel = AssistantViewModel(commandExecutor, ttsFeedbackManager, commandHistoryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `processCommand updates state to Success on successful execution`() =
            runTest(testDispatcher) {
                val command = "open settings"
                val feedback = "Opening Settings."

                coEvery { commandExecutor.execute(command) } returns
                        CommandResult(executionResult = ExecutionResult.Success, displayName = "Settings", actionId = "OPEN")

                viewModel.state.test {
                    // Initial state
                    assertEquals(AssistantState.Idle, awaitItem())

                    viewModel.onTypedCommandChange(command)
                    viewModel.onTypedCommandSubmit()

                    // Skip Processing state if it happens too fast or check for it
                    // Based on implementation: _state.value = AssistantState.Processing(rawText)
                    val processingItem = awaitItem()
                    assertTrue(processingItem is AssistantState.Processing)
                    assertEquals(
                            command,
                            (processingItem as AssistantState.Processing).recognizedText
                    )

                    // Success state
                    val successItem = awaitItem()
                    assertTrue(successItem is AssistantState.Success)
                    assertEquals(command, (successItem as AssistantState.Success).originalCommand)
                    assertEquals(feedback, successItem.feedbackMessage)

                    verify { ttsFeedbackManager.speak(feedback) }

                    // Should eventually return to Idle
                    assertEquals(AssistantState.Idle, awaitItem())
                }
            }

    @Test
    fun `processCommand updates state to Error on failure`() =
            runTest(testDispatcher) {
                val command = "unknown command"
                val feedback = "Something went wrong: Unknown"

                coEvery { commandExecutor.execute(command) } returns CommandResult(
                        ExecutionResult.Failure.ExecutionException(RuntimeException("Unknown")))

                viewModel.state.test {
                    assertEquals(AssistantState.Idle, awaitItem())

                    viewModel.onTypedCommandChange(command)
                    viewModel.onTypedCommandSubmit()

                    val processingItem = awaitItem()
                    assertTrue(processingItem is AssistantState.Processing)

                    val errorItem = awaitItem()
                    assertTrue(errorItem is AssistantState.Error)
                    assertEquals(command, (errorItem as AssistantState.Error).originalCommand)
                    assertEquals(feedback, errorItem.errorMessage)

                    verify { ttsFeedbackManager.speak(feedback) }

                    // Should eventually return to Idle
                    assertEquals(AssistantState.Idle, awaitItem())
                }
            }

    @Test
    fun `onSpeechError updates state to Error and speaks message`() =
            runTest(testDispatcher) {
                val errorMessage = "Speech recognition failed"

                viewModel.state.test {
                    assertEquals(AssistantState.Idle, awaitItem())

                    viewModel.onSpeechError(errorMessage)

                    val errorItem = awaitItem()
                    assertTrue(errorItem is AssistantState.Error)
                    assertEquals(errorMessage, (errorItem as AssistantState.Error).errorMessage)

                    verify { ttsFeedbackManager.speak(errorMessage) }

                    // Should eventually return to Idle
                    assertEquals(AssistantState.Idle, awaitItem())
                }
            }
}
