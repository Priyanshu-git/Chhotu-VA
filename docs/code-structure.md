# Code Structure

## Package Hierarchy

```
com.nexxlabs.chhotu/
│
├── ChhotuApplication.kt          # @HiltAndroidApp entry point
│
├── ui/                           # Presentation layer
│   ├── MainActivity.kt
│   ├── AppNavGraph.kt
│   ├── AssistantScreen.kt
│   ├── AssistantState.kt         # Sealed class for UI state machine
│   ├── AssistantViewModel.kt
│   ├── SettingsScreen.kt
│   ├── SettingsViewModel.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       ├── ThemeMode.kt          # Enum: LIGHT, DARK, SYSTEM
│       └── Type.kt
│
├── domain/                       # Pure Kotlin business logic
│   ├── constants/
│   │   ├── ActionIds.kt          # String constants for action identifiers
│   │   └── EntityKeys.kt         # String constants for entity map keys
│   ├── engine/
│   │   ├── EngineInterface.kt    # suspend fun analyze(command): StructuredIntent
│   │   ├── EngineUtil.kt         # fallbackIntent() helper
│   │   ├── CommandNormalizer.kt
│   │   ├── CapabilityResolver.kt
│   │   ├── ContactManager.kt
│   │   ├── ai/
│   │   │   ├── AIIntentEngine.kt
│   │   │   └── model/
│   │   │       ├── IntentType.kt      # Enum: OPEN_APP, APP_ACTION, SYSTEM_ACTION, UNKNOWN
│   │   │       └── StructuredIntent.kt
│   │   └── rule/
│   │       └── BasicEngine.kt
│   ├── model/
│   │   └── ContactModel.kt       # data class Contact(name, phoneNumber)
│   ├── platform/                 # Android-agnostic interfaces
│   │   ├── IntentLauncher.kt
│   │   ├── AppInstallationChecker.kt
│   │   └── SystemServiceProvider.kt
│   ├── registry/
│   │   ├── AppRegistry.kt        # interface: getAllEntries(), findEntry()
│   │   ├── Executable.kt         # fun interface: execute(intent, context): ExecutionResult
│   │   ├── StaticAppRegistry.kt  # 60+ app definitions
│   │   ├── executables/
│   │   │   ├── IntentExecutable.kt
│   │   │   ├── SystemExecutable.kt
│   │   │   ├── DeepLinkExecutable.kt
│   │   │   ├── VolumeExecutable.kt
│   │   │   └── FlashlightExecutable.kt
│   │   └── model/
│   │       ├── Action.kt
│   │       ├── ActionContract.kt
│   │       ├── CommandResult.kt
│   │       ├── ExecutionResult.kt
│   │       └── RegistryEntry.kt
│   ├── repository/
│   │   └── ContactRepository.kt  # interface
│   └── usecase/
│       ├── ExecuteVoiceCommandUseCase.kt
│       └── FeedbackMessageGenerator.kt
│
├── execution/
│   └── CommandExecutor.kt        # Thin facade (backward compat)
│
├── data/                         # Android implementations + I/O
│   ├── contacts/
│   │   └── ContactRepositoryImpl.kt
│   ├── local/
│   │   ├── AppDataStore.kt       # DataStore wrapper + all key constants
│   │   ├── CommandHistoryItem.kt
│   │   ├── CommandHistoryRepository.kt
│   │   └── SettingsRepository.kt
│   ├── platform/
│   │   ├── AndroidIntentLauncher.kt
│   │   ├── AndroidAppInstallationChecker.kt
│   │   └── AndroidSystemServiceProvider.kt
│   └── remote/
│       ├── LLMService.kt
│       └── model/
│           └── LLMModels.kt      # ChatCompletionRequest, ChatCompletionResponse, Message
│
├── speech/
│   ├── SpeechInputManager.kt
│   └── TTSFeedbackManager.kt
│
├── di/
│   ├── AppModule.kt
│   └── RegistryModule.kt
│
└── util/
    └── Constants.kt              # Log tags, API base URL
```

---

## Layer Responsibilities

### `ui/`
Owns user interaction and visual state. Observes `StateFlow` from ViewModels; emits user events (mic tap, text submit, contact selection). No direct access to `data/` or `domain/` beyond ViewModels.

### `domain/`
Pure Kotlin — no `android.*` imports (except in `domain/platform/` interfaces, which are abstractions). This layer defines _what_ the app does, not _how_ Android executes it. All classes here are unit-testable without Robolectric.

### `data/`
Implements domain interfaces using Android APIs (ContentProvider, DataStore, Retrofit, CameraManager, etc.). Bridges the domain's platform interfaces with real Android implementations.

### `speech/`
Thin wrappers around Android's speech subsystem. Both managers are `@Singleton` and provided via Hilt.

### `di/`
Wires everything together. `AppModule` uses `@Provides`; `RegistryModule` uses `@Binds` for zero-overhead interface binding.

---

## Naming Conventions

| Concept | Convention | Example |
|---|---|---|
| ViewModels | `*ViewModel` | `AssistantViewModel` |
| Compose screens | `*Screen` | `AssistantScreen` |
| UI state | `*State` | `AssistantState` |
| Repositories | `*Repository` | `CommandHistoryRepository` |
| Use cases | `*UseCase` or descriptive noun | `ExecuteVoiceCommandUseCase`, `FeedbackMessageGenerator` |
| Executables | `*Executable` | `IntentExecutable` |
| Platform interfaces | descriptive noun | `IntentLauncher` |
| Android implementations | `Android*` | `AndroidIntentLauncher` |
| Action ID constants | `SCREAMING_SNAKE_CASE` in `ActionIds` | `ActionIds.OPEN`, `ActionIds.CALL` |
| Entity keys | `SCREAMING_SNAKE_CASE` in `EntityKeys` | `EntityKeys.CONTACT`, `EntityKeys.QUERY` |
| DataStore keys | `SCREAMING_SNAKE_CASE` in `AppDataStore.Keys` | `AppDataStore.THEME_MODE` |

---

## Key Interfaces

```kotlin
// All intent engines share one contract
interface EngineInterface {
    suspend fun analyze(command: String): StructuredIntent
}

// All executables share one contract
fun interface Executable {
    fun execute(intent: StructuredIntent, context: Context): ExecutionResult
}

// App registry
interface AppRegistry {
    fun getAllEntries(): List<RegistryEntry>
    fun findEntry(appName: String): RegistryEntry?
}
```
