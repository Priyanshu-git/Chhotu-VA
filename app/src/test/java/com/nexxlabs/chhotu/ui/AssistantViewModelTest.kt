package com.nexxlabs.chhotu.ui

import android.util.Log
import app.cash.turbine.test
import com.nexxlabs.chhotu.data.local.CommandHistoryRepository
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.execution.CommandExecutor
import com.nexxlabs.chhotu.speech.TTSFeedbackManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    private lateinit var commandHistoryRepository: CommandHistoryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0

        commandExecutor = mockk()
        ttsFeedbackManager = mockk(relaxed = true)
        commandHistoryRepository = mockk(relaxed = true)

        // Mock history flow
        every { commandHistoryRepository.history } returns flowOf(emptyList())

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

    @Test
    fun `processCommand updates state to SelectContact on AmbiguousContact failure`() =
            runTest(testDispatcher) {
                val command = "call john"
                val contacts = listOf(
                        com.nexxlabs.chhotu.domain.model.Contact("John 1", "123"),
                        com.nexxlabs.chhotu.domain.model.Contact("John 2", "456")
                )
                val intent = StructuredIntent(IntentType.APP_ACTION, "phone", "CALL", emptyMap(), 1.0)

                coEvery { commandExecutor.execute(command) } returns CommandResult(
                        executionResult = ExecutionResult.Failure.AmbiguousContact(contacts),
                        intent = intent
                )

                viewModel.state.test {
                    assertEquals(AssistantState.Idle, awaitItem())

                    viewModel.onTypedCommandSubmit() // blank command skip
                    viewModel.onTypedCommandChange(command)
                    viewModel.onTypedCommandSubmit()

                    assertTrue(awaitItem() is AssistantState.Processing)

                    val selectState = awaitItem()
                    assertTrue(selectState is AssistantState.SelectContact)
                    assertEquals(contacts, (selectState as AssistantState.SelectContact).contacts)
                    assertEquals(intent, selectState.intent)

                    // Should NOT return to Idle automatically
                    expectNoEvents()
                }
            }

    @Test
    fun `onContactSelected re-executes intent with specialized path`() =
            runTest(testDispatcher) {
                val command = "call john"
                val contact = com.nexxlabs.chhotu.domain.model.Contact("John 1", "123")
                val intent = StructuredIntent(IntentType.APP_ACTION, "phone", "CALL", mutableMapOf("contact" to "john"), 1.0)

                // 1. Initial ambiguity
                coEvery { commandExecutor.execute(command) } returns CommandResult(
                        executionResult = ExecutionResult.Failure.AmbiguousContact(listOf(contact)),
                        intent = intent
                )

                // 2. Selection execution
                coEvery { commandExecutor.executeIntent(any()) } returns CommandResult(
                        executionResult = ExecutionResult.Success,
                        displayName = "Phone",
                        actionId = "CALL"
                )

                viewModel.state.test {
                    assertEquals(AssistantState.Idle, awaitItem())

                    viewModel.onTypedCommandChange(command)
                    viewModel.onTypedCommandSubmit()

                    assertTrue(awaitItem() is AssistantState.Processing)
                    assertTrue(awaitItem() is AssistantState.SelectContact)

                    // Act: Select the contact
                    viewModel.onContactSelected(contact)

                    // Verify specialized execution
                    assertTrue(awaitItem() is AssistantState.Processing)
                    assertTrue(awaitItem() is AssistantState.Success)

                    coVerify { commandExecutor.executeIntent(match { 
                        it.entities["contact_number"] == "123" && it.entities["contact"] == "John 1"
                    })}
                    
                    assertEquals(AssistantState.Idle, awaitItem())
                }
            }
}
