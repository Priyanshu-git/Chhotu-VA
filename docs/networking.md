# Networking

## Overview

Chhotu makes a single type of outbound network call: the AI intent classification request to an OpenAI-compatible chat completions endpoint. All networking is done via **Retrofit 2 + OkHttp 4 + Gson**.

---

## Libraries

| Library | Version | Role |
|---|---|---|
| Retrofit 2 | 2.11.0 | HTTP client abstraction |
| OkHttp 4 | 4.12.0 | Underlying HTTP engine |
| OkHttp Logging Interceptor | 4.12.0 | Debug-mode request/response logging |
| Gson Converter | 2.11.0 | JSON serialisation |

---

## Configuration

Retrofit is configured in `AppModule.kt`:

```kotlin
@Provides @Singleton
fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
    Retrofit.Builder()
        .baseUrl(BASE_URL)           // BuildConfig.LLM_BASE_URL from local.properties
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
```

`BASE_URL` is read from `Constants.API.BASE_URL`, which is set from `BuildConfig.LLM_BASE_URL`.

OkHttp is configured with a `HttpLoggingInterceptor`:

```kotlin
level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else Level.NONE
```

---

## API Interface

```kotlin
interface LLMService {
    @POST("v1/chat/completions")
    suspend fun getCompletions(
        @Header("Authorization") authorization: String,   // "Bearer <key>"
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
}
```

The endpoint is OpenAI-compatible — any provider implementing the `/v1/chat/completions` spec works.

---

## Request / Response Models

### Request

```kotlin
data class ChatCompletionRequest(
    val model: String,              // e.g. "openai/gpt-4o-mini"
    val messages: List<Message>
)

data class Message(
    val role: String,               // "user" | "system" | "assistant"
    val content: String
)
```

### Response

```kotlin
data class ChatCompletionResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
```

---

## Request Construction

`AIIntentEngine` sends a single `user` message containing both the system prompt and the command:

```kotlin
val messages = listOf(Message(
    role = "user",
    content = getSystemPrompt() + "\n\nCommand: " + command
))
```

The system prompt instructs the model to:
1. Output a `StructuredIntent` JSON
2. Use only apps and aliases from the live `StaticAppRegistry`
3. Return `STRICT JSON OUTPUT ONLY. NO MARKDOWN. NO EXPLANATION.`

---

## Response Parsing

The raw `content` string from the LLM is parsed in `parseResponseContent()`:

```kotlin
// Strip markdown code fences if the model wraps output anyway
val cleanJson = when {
    content.startsWith("```json") -> content.removePrefix("```json").removeSuffix("```").trim()
    content.startsWith("```")     -> content.removePrefix("```").removeSuffix("```").trim()
    else                          -> content
}
val intent = gson.fromJson(cleanJson, StructuredIntent::class.java)
```

---

## Error Handling Strategy

| Condition | Handling |
|---|---|
| `response.isSuccessful == false` | Log error, return `fallbackIntent()` (UNKNOWN) |
| Empty `choices` list | Return `fallbackIntent()` |
| JSON parse exception | Log error, return `fallbackIntent()` |
| `confidence < 0.6` | Log warning, return `fallbackIntent()` |
| Any `Exception` in coroutine | Catch-all, log, return `fallbackIntent()` |

`fallbackIntent()` returns a `StructuredIntent` with `intentType = UNKNOWN`, which propagates to `CapabilityResolver` and results in an `ActionNotSupported` execution result with user-facing TTS feedback.

All LLM calls run on `Dispatchers.IO` via `withContext(Dispatchers.IO)`.

---

## API Endpoint Summary

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/v1/chat/completions` | `Bearer <LLM_API_KEY>` | Classify a voice command into a `StructuredIntent` |

No other external endpoints are used.
