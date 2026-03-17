# Components

## Activities

### `MainActivity`

The single activity. Hosts the Navigation Compose graph and handles runtime permission requests (`RECORD_AUDIO`, `READ_CONTACTS`, etc.).

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

---

## Compose Screens

### `AssistantScreen`

The main screen. Observes `AssistantViewModel.state: StateFlow<AssistantState>` and renders the appropriate UI:

- **Idle** — mic button and text input
- **Listening** — animated listening indicator
- **Processing** — progress indicator with the recognized text
- **Success** — confirmation message with feedback text
- **Error** — error card with message
- **SelectContact** — bottom sheet listing matched contacts for disambiguation

Also observes `commandHistory` and `showOnboarding` StateFlows.

### `SettingsScreen`

Allows users to configure:
- Theme (Light / Dark / System)
- Speech recognition language

Observes `SettingsViewModel`.

---

## ViewModels

### `AssistantViewModel`

```kotlin
@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val ttsFeedbackManager: TTSFeedbackManager,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val feedbackMessageGenerator: FeedbackMessageGenerator,
    private val settingsRepository: SettingsRepository
) : ViewModel()
```

**Exposed StateFlows:**

| Property | Type | Description |
|---|---|---|
| `state` | `StateFlow<AssistantState>` | Current UI state |
| `commandHistory` | `StateFlow<List<CommandHistoryItem>>` | Persisted history |
| `typedCommand` | `StateFlow<String>` | Text field binding |
| `showOnboarding` | `StateFlow<Boolean>` | First-launch flag |

**Key methods:**

| Method | Trigger | Effect |
|---|---|---|
| `onStartListening()` | Mic button tap | State → Listening |
| `onSpeechRecognized(text)` | STT result | Runs pipeline |
| `onSpeechError(msg)` | STT error | State → Error, auto-reset after 3s |
| `onTypedCommandSubmit()` | Send button | Runs pipeline with typed input |
| `onContactSelected(contact)` | Contact picker | Re-executes intent with resolved contact |
| `completeOnboarding()` | Onboarding finish | Persists flag; hides onboarding |
| `deleteHistoryItem(item)` | Swipe-to-delete | Removes from DataStore |

### `SettingsViewModel`

Exposes theme mode and speech language as StateFlows; delegates persistence to `SettingsRepository`.

---

## Repositories

### `CommandHistoryRepository`

Persists command history to DataStore as a JSON-serialised list. Exposes `history: Flow<List<CommandHistoryItem>>`.

```kotlin
data class CommandHistoryItem(
    val originalText: String,
    val intentType: String,
    val wasSuccessful: Boolean,
    val feedbackMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### `SettingsRepository`

DataStore-backed key-value store for user preferences.

| Property | Key | Default |
|---|---|---|
| `themeMode` | `theme_mode` | `SYSTEM` |
| `hasCompletedOnboarding` | `onboarding_completed` | `false` |
| `speechLanguage` | `speech_language` | `"System default"` |

### `ContactRepositoryImpl`

Queries the Android Contacts ContentProvider. Used by `ContactManager` to resolve contact names to phone numbers.

---

## Use Cases

### `ExecuteVoiceCommandUseCase`

The core pipeline in a single `suspend fun execute(rawCommand: String): CommandResult`:

1. Normalize the raw input
2. Try `BasicEngine` — if `UNKNOWN`, fall through to `AIIntentEngine`
3. Resolve the intent via `CapabilityResolver`
4. Return a `CommandResult`

Also exposes `fun executeIntent(intent: StructuredIntent): CommandResult` for pre-resolved intents (contact disambiguation re-execution path).

### `FeedbackMessageGenerator`

Maps a `CommandResult` to a human-readable string for TTS output:

```kotlin
// Examples of generated feedback
"Opening WhatsApp."
"Searching on Spotify."
"Calling via Phone."
"Volume increased."
"Zomato is not installed on your device."
"Multiple contacts found. Which one would you like to use?"
```

---

## Executables

### `IntentExecutable`

Builds and launches a standard `Intent` (open app, browser URL). Uses `IntentLauncher` for the Android call.

### `SystemExecutable`

Handles intents that require entity resolution before launching — specifically phone calls and SMS where the contact entity is a name, not a number. Delegates contact lookup to `ContactManager`.

Supports URI templates for dynamic target construction.

### `DeepLinkExecutable`

Launches deep links with `{placeholder}` interpolation from the `entities` map. Falls back to a configured fallback executable if the app is not installed.

```kotlin
// Example: "spotify://search/{query}" with entities = { "query": "Bakhuda" }
// Resolves to: "spotify://search/Bakhuda"
```

### `VolumeExecutable`

Uses `AudioManager` (via `SystemServiceProvider`) to raise, lower, or mute media volume.

### `FlashlightExecutable`

Uses `CameraManager` (via `SystemServiceProvider`) to toggle the device torch.

---

## Speech

### `SpeechInputManager`

Wraps `android.speech.SpeechRecognizer`. Exposes callbacks:
- `onResults(text)` — forwarded to `AssistantViewModel.onSpeechRecognized()`
- `onError(message)` — forwarded to `AssistantViewModel.onSpeechError()`

### `TTSFeedbackManager`

Wraps `android.speech.tts.TextToSpeech`. Exposes:
- `speak(text)` — queues utterance
- `shutdown()` — called in `ViewModel.onCleared()`

---

## App Registry

### `StaticAppRegistry`

Defines 60+ `RegistryEntry` objects at compile time. Each entry follows this structure:

```kotlin
RegistryEntry(
    appId      = "whatsapp",
    displayName = "WhatsApp",
    packageName = "com.whatsapp",
    aliases    = setOf("whatsapp", "wa"),
    actions    = setOf(
        Action(
            id      = ActionIds.OPEN,
            aliases = setOf("open", "launch"),
            contract = ActionContract(requiredEntities = emptySet()),
            primaryExecutable = IntentExecutable(...)
        ),
        Action(
            id      = ActionIds.SEND_MESSAGE,
            aliases = setOf("send", "message", "text"),
            contract = ActionContract(requiredEntities = setOf(EntityKeys.CONTACT, EntityKeys.MESSAGE)),
            primaryExecutable = SystemExecutable(...)
        )
    )
)
```

`CapabilityResolver` calls `findEntry(targetApp)` then matches the `action` string against each `Action`'s `aliases`.
