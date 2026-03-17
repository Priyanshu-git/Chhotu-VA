# Setup Guide

## Environment Requirements

| Requirement | Minimum |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or later |
| JDK | 11 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.0.21 |
| Device / Emulator | API 26 (Android 8.0 Oreo) |
| LLM API | Any OpenAI-compatible endpoint |

---

## Installation Steps

### 1. Clone the repository

```bash
git clone https://github.com/your-org/chhotu-va.git
cd chhotu-va
```

### 2. Configure API credentials

Create `local.properties` in the **project root** (the same directory as `settings.gradle`). This file is excluded from version control.

```properties
# Required: your LLM API key
LLM_API_KEY=sk-or-v1-xxxxxxxxxxxxxxxxxxxx

# Required: base URL of your OpenAI-compatible endpoint (include trailing slash)
LLM_BASE_URL=https://openrouter.ai/api/
```

These values are injected into `BuildConfig` at compile time:
- `BuildConfig.LLM_API_KEY`
- `BuildConfig.LLM_BASE_URL`

> **Recommended providers:** OpenRouter (gives access to many models), OpenAI directly, or any self-hosted OpenAI-compatible server.

### 3. Open in Android Studio

- **File → Open** and select the `chhotu-va` folder.
- Wait for Gradle sync to complete.
- Android Studio will download all dependencies automatically.

### 4. Grant runtime permissions (first run)

The app requests the following permissions at runtime:

| Permission | Purpose |
|---|---|
| `RECORD_AUDIO` | Voice input via SpeechRecognizer |
| `READ_CONTACTS` | Contact lookup for call/SMS commands |
| `CALL_PHONE` | Direct phone calls |
| `SEND_SMS` | Sending SMS messages |
| `INTERNET` | LLM API calls |

---

## Gradle Configuration Notes

### Version catalog

All dependency versions are centralised in `gradle/libs.versions.toml`. To upgrade a library, change its version there — no need to touch `build.gradle`.

```toml
[versions]
kotlin = "2.0.21"
hilt  = "2.57.1"
room  = "2.8.3"
# ...
```

### Build config fields

`app/build.gradle` reads from `local.properties` and exposes values as typed `BuildConfig` fields:

```groovy
buildConfigField "String", "LLM_API_KEY",  "\"${myApiKey}\""
buildConfigField "String", "LLM_BASE_URL", "\"${myBaseUrl}\""
```

### Build types

| Type | Minification | Logging |
|---|---|---|
| `debug` | Off | OkHttp BODY-level logging |
| `release` | On (R8 + resource shrinking) | No logging |

---

## Switching LLM Models

The AI engine uses the model string defined in `AIIntentEngine.kt`:

```kotlin
private const val MODEL = "openai/gpt-4o-mini"
```

Change this constant to use a different model. For OpenRouter, any model slug from their catalogue works (e.g., `x-ai/grok-4.1-fast`, `anthropic/claude-haiku-3-5`).

---

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---|---|---|
| Gradle sync fails | Missing `local.properties` | Create the file with `LLM_API_KEY` and `LLM_BASE_URL` |
| AI commands always fail | Wrong API key or base URL | Verify credentials in `local.properties` |
| Voice input not working | `RECORD_AUDIO` denied | Grant permission in device Settings |
| "App not installed" on every command | App queries not declared | Check `AndroidManifest.xml` `<queries>` block |
| Build error: `release(36)` | AGP < 8.x | Upgrade Android Gradle Plugin to 8.13.2+ |
