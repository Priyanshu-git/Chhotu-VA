# Overview

## What is Chhotu?

Chhotu is a voice assistant Android app that lets users control their phone and launch actions in third-party apps using natural language — either spoken or typed. The name "Chhotu" is a colloquial Hindi term for a small helper or assistant.

Unlike cloud-dependent assistants, Chhotu is designed to be lean: a fast rule-based engine handles common commands locally, while an AI-powered LLM fallback handles complex or ambiguous phrasing.

---

## Purpose and Target Users

**Target users:** Android users who want quick, hands-free control over apps — particularly useful while driving, cooking, or when hands are occupied.

**Primary goals:**
- Reduce friction in launching apps and performing actions across 60+ popular applications
- Handle natural language variation gracefully (e.g., "play something on Spotify", "ring John", "order food")
- Provide instant voice feedback so users never need to look at the screen

---

## Supported App Categories

| Category | Examples |
|---|---|
| Social & Messaging | WhatsApp, Instagram, Telegram, Snapchat, Threads |
| Google Apps | Maps, Photos, Drive, Meet, Calendar, Calculator |
| Music & Video | Spotify, YouTube, YouTube Music, Netflix, Hotstar, JioCinema |
| Shopping | Amazon, Flipkart, Myntra, Meesho, AJIO |
| Food & Grocery | Zomato, Swiggy, Blinkit, Zepto, BigBasket |
| Payments (UPI) | PhonePe, Google Pay, Paytm, BHIM |
| Travel | Uber, Ola, Rapido, IRCTC, MakeMyTrip |
| Productivity | Microsoft Teams, Zoom, Canva, CapCut, ChatGPT |
| System | Phone (calls), Volume, Flashlight, SMS |

---

## Key Workflows

### 1. Voice Command — Happy Path

```
User taps mic
    → App transitions to "Listening" state
    → Android SpeechRecognizer captures speech
    → Text is passed to the processing pipeline
    → Command is executed (app launched, action performed)
    → TTS feedback is spoken to the user
    → State returns to Idle
```

### 2. Text Command

```
User types in the text field and taps Send
    → Same processing pipeline as voice
    → TTS feedback + state transitions identical
```

### 3. Ambiguous Contact Resolution

When a command like "call John" matches multiple contacts:

```
Pipeline returns AmbiguousContact result
    → UI transitions to SelectContact state
    → Bottom sheet displays matching contacts
    → User taps the correct contact
    → Intent is re-executed with resolved contact details
    → TTS confirms the call
```

### 4. App Not Installed

```
Command targets an app that is not installed
    → ExecutionResult.Failure.AppNotInstalled is returned
    → TTS: "Zomato is not installed on your device."
    → (optional) Deep-link fallback may redirect to Play Store
```

### 5. Onboarding

On first launch:

```
SettingsRepository reports hasCompletedOnboarding = false
    → OnboardingScreen is shown over AssistantScreen
    → User completes the walkthrough
    → setOnboardingCompleted() is called
    → Onboarding is never shown again
```
