# Testing

## Strategy

Tests are split into two suites:

| Suite | Location | Runner |
|---|---|---|
| Unit tests | `app/src/test/` | Standard JVM (no device) |
| Instrumented tests | `app/src/androidTest/` | `CustomTestRunner` (Hilt) |

The unit test suite is the primary focus. All domain logic, executables, engines, and ViewModels are unit-tested without requiring an Android device.

---

## Tools

| Tool | Version | Purpose |
|---|---|---|
| JUnit 4 | 4.13.2 | Test runner and assertions |
| MockK | 1.13.10 | Kotlin-idiomatic mocking |
| Turbine | 1.1.0 | Testing `Flow` / `StateFlow` emissions |
| Kotlin Coroutines Test | 1.8.1 | `StandardTestDispatcher`, `runTest` |
| Hilt Android Testing | 2.57.1 | Hilt injection in instrumented tests |
| Espresso | 3.5.1 | UI instrumented tests |
| `org.json` | 20230227 | JSON parsing in AI engine tests |

---

## Running Tests

```bash
# All unit tests
./gradlew testDebugUnitTest

# Single test class
./gradlew testDebugUnitTest --tests "com.nexxlabs.chhotu.ui.AssistantViewModelTest"

# Single test method (wildcards supported)
./gradlew testDebugUnitTest --tests "com.nexxlabs.chhotu.ui.AssistantViewModelTest.processCommand*"

# Instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest
```

---

## Unit Test Files

| Test File | What It Tests |
|---|---|
| `AssistantViewModelTest` | Full state machine: Idle→Processing→Success/Error/SelectContact |
| `ExecuteVoiceCommandUseCaseTest` | Pipeline orchestration (engine selection, fallback) |
| `AIIntentEngineTest` | LLM response parsing, confidence threshold, markdown stripping |
| `BasicEngineTest` | Regex token matching for `open`, `call`, fallback |
| `CapabilityResolverTest` | Registry lookup and executable dispatch |
| `StaticAppRegistryTest` | Registry integrity — entries have required fields |
| `SystemExecutableTest` | Contact resolution + URI construction |
| `VolumeExecutableTest` | AudioManager interaction |
| `ContactManagerTest` | Disambiguation logic |
| `CommandNormalizerTest` | Text normalisation rules |
| `FeedbackMessageGeneratorTest` | All `ExecutionResult` variants → correct string |

---

## Key Patterns

### ViewModels — Turbine + StandardTestDispatcher

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before fun setup() {
        Dispatchers.setMain(testDispatcher)
        // mock Android.util.Log to avoid crashes on JVM
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `processCommand updates state to Success`() = runTest(testDispatcher) {
        coEvery { commandExecutor.execute("open settings") } returns
            CommandResult(ExecutionResult.Success, displayName = "Settings", actionId = "OPEN")

        viewModel.state.test {
            assertEquals(AssistantState.Idle, awaitItem())
            viewModel.onTypedCommandChange("open settings")
            viewModel.onTypedCommandSubmit()
            assertTrue(awaitItem() is AssistantState.Processing)
            val success = awaitItem() as AssistantState.Success
            assertEquals("Opening Settings.", success.feedbackMessage)
            assertEquals(AssistantState.Idle, awaitItem())
        }
    }
}
```

### AI Engine — Mocked LLM + Markdown Parsing

`AIIntentEngineTest` mocks `LLMService` using MockK and tests that:
- Valid JSON responses are parsed correctly
- Markdown-wrapped responses (`` ```json `` fences) are cleaned and parsed
- Responses with `confidence < 0.6` return `fallbackIntent()`
- HTTP error responses return `fallbackIntent()`

### Hilt Instrumented Tests

The custom test runner is required to replace the real Hilt component:

```kotlin
// CustomTestRunner.kt
class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, name: String, context: Context) =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
```

Instrumented tests annotate with `@HiltAndroidTest` and use `@BindValue` or test modules to override dependencies.

---

## What Is NOT Mocked

- `FeedbackMessageGenerator` — used directly (no side effects, pure function)
- `StructuredIntent` — constructed directly in tests
- `ExecutionResult` — constructed directly in tests

## Conventions

- Use `coEvery` / `coVerify` (MockK) for `suspend fun`
- Use `every` / `verify` for regular functions
- Use `relaxed = true` on mocks that need to accept calls without explicit stubbing (e.g., `TTSFeedbackManager`)
- `mockkStatic(Log::class)` is required in every ViewModel test to prevent crashes from `android.util.Log`
