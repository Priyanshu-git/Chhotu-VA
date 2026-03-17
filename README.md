# Chhotu — Voice Assistant for Android

> A voice-first Android assistant that understands natural language commands and executes actions across 60+ popular apps using a hybrid decision engine (rule-based + AI).

---

## Screenshots

| Idle / Onboarding | Listening | Success | Contact Picker |
|---|---|---|---|
| *(placeholder)* | *(placeholder)* | *(placeholder)* | *(placeholder)* |

---

## Features

- **Voice & text input** — speak a command or type it in
- **Hybrid intent engine** — fast rule-based parsing first; AI fallback for complex commands
- **60+ supported apps** — WhatsApp, YouTube, Spotify, Zomato, PhonePe, Uber, and many more
- **System actions** — volume control, flashlight toggle, phone calls, SMS
- **Contact disambiguation** — intelligently resolves ambiguous contacts and prompts selection
- **TTS feedback** — speaks back every result
- **Command history** — persisted locally; view and delete past commands
- **Theming** — Light / Dark / System theme modes
- **Onboarding flow** — first-launch walkthrough
- **Configurable LLM backend** — point at any OpenAI-compatible endpoint

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| Min / Target SDK | 26 / 36 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Hilt 2.57.1 |
| Async | Kotlin Coroutines + StateFlow |
| Networking | Retrofit 2.11 + OkHttp 4.12 + Gson |
| Local Storage | Room 2.8 + DataStore Preferences 1.1 |
| AI / LLM | OpenAI-compatible API (default: `openai/gpt-4o-mini`) |
| Speech | Android SpeechRecognizer (STT) + TextToSpeech (TTS) |
| Navigation | Navigation Compose 2.9 |
| Testing | JUnit 4, MockK, Turbine, Coroutines Test, Espresso |

---

## Architecture Overview

Chhotu follows **MVVM + Clean Architecture** with a strict layering policy:

```
ui/          ← Compose screens + ViewModels
domain/      ← Business logic (engines, registry, executables, use cases)
data/        ← Retrofit, Room, DataStore, platform implementations
execution/   ← Thin facade over use cases
speech/      ← STT + TTS managers
di/          ← Hilt modules
```

The core **command processing pipeline**:

```
Voice/Text Input
      │
      ▼
CommandNormalizer
      │
      ▼
BasicEngine ──(UNKNOWN)──► AIIntentEngine (LLM)
      │                           │
      └───────────────────────────┘
                  │
                  ▼
          CapabilityResolver
                  │
                  ▼
           Executable.execute()
                  │
            ExecutionResult
```

See [`docs/architecture.md`](docs/architecture.md) for a full breakdown.

---

## Setup

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11+
- Android device or emulator (API 26+)
- An OpenAI-compatible LLM API key (OpenRouter, OpenAI, etc.)

### Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/your-org/chhotu-va.git
   cd chhotu-va
   ```

2. **Add API credentials** — create `local.properties` in the project root (alongside `gradle.properties`):
   ```properties
   LLM_API_KEY=sk-your-api-key-here
   LLM_BASE_URL=https://openrouter.ai/api/
   ```

3. **Open in Android Studio** — let Gradle sync complete.

4. **Run on device / emulator**
   ```bash
   ./gradlew installDebug
   ```

See [`docs/setup.md`](docs/setup.md) for detailed environment configuration.

---

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Release build (minification + resource shrinking enabled)
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Run a single test class
./gradlew testDebugUnitTest --tests "com.nexxlabs.chhotu.ui.AssistantViewModelTest"

# Instrumented tests (requires connected device)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

---

## Folder Structure

```
app/src/main/java/com/nexxlabs/chhotu/
├── ChhotuApplication.kt        # Hilt application entry point
├── data/
│   ├── contacts/               # ContactRepositoryImpl
│   ├── local/                  # AppDataStore, Room, SettingsRepository, CommandHistoryRepository
│   ├── platform/               # Android implementations of domain platform interfaces
│   └── remote/                 # LLMService (Retrofit), request/response models
├── di/
│   ├── AppModule.kt            # Singleton providers (Retrofit, TTS, DataStore…)
│   └── RegistryModule.kt       # @Binds for interface → implementation
├── domain/
│   ├── constants/              # ActionIds, EntityKeys
│   ├── engine/
│   │   ├── ai/                 # AIIntentEngine + StructuredIntent model
│   │   ├── rule/               # BasicEngine
│   │   ├── CapabilityResolver.kt
│   │   ├── CommandNormalizer.kt
│   │   └── ContactManager.kt
│   ├── model/                  # Contact
│   ├── platform/               # IntentLauncher, AppInstallationChecker, SystemServiceProvider (interfaces)
│   ├── registry/
│   │   ├── executables/        # IntentExecutable, SystemExecutable, DeepLinkExecutable, VolumeExecutable, FlashlightExecutable
│   │   ├── model/              # Action, ActionContract, RegistryEntry, ExecutionResult, CommandResult
│   │   ├── AppRegistry.kt      # Interface
│   │   └── StaticAppRegistry.kt
│   ├── repository/             # ContactRepository (interface)
│   └── usecase/                # ExecuteVoiceCommandUseCase, FeedbackMessageGenerator
├── execution/
│   └── CommandExecutor.kt      # Thin facade
├── speech/
│   ├── SpeechInputManager.kt   # Android STT wrapper
│   └── TTSFeedbackManager.kt   # Android TTS wrapper
└── ui/
    ├── AssistantScreen.kt
    ├── AssistantState.kt
    ├── AssistantViewModel.kt
    ├── SettingsScreen.kt
    ├── SettingsViewModel.kt
    ├── AppNavGraph.kt
    ├── MainActivity.kt
    └── theme/                  # Color, Type, Theme, ThemeMode
```

---

## Contribution Guidelines

1. Fork the repository and create a feature branch from `master`.
2. Follow the existing package structure and layering rules — no cross-layer leakage (e.g., `data` must not be imported from `domain`).
3. Add unit tests for any new engine, executable, or use-case logic.
4. Run `./gradlew testDebugUnitTest lint` before opening a PR.
5. Keep commits atomic with descriptive messages.

---

## License

This project is licensed under the [MIT License](LICENSE).
