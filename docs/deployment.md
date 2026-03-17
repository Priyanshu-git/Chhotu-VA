# Deployment

## Build Variants

| Variant | Minification | Resource Shrinking | OkHttp Logging |
|---|---|---|---|
| `debug` | Off | Off | `BODY` (full request/response) |
| `release` | On (R8) | On | None |

---

## Generating a Debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

Install directly on a connected device:

```bash
./gradlew installDebug
# or
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Generating a Release APK / AAB

### 1. Create a signing keystore (first time only)

```bash
keytool -genkeypair -v \
  -keystore chhotu-release.jks \
  -alias chhotu \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```

### 2. Add signing config to `local.properties`

```properties
KEYSTORE_PATH=../chhotu-release.jks
KEYSTORE_PASSWORD=your-store-password
KEY_ALIAS=chhotu
KEY_PASSWORD=your-key-password
```

### 3. Reference in `app/build.gradle` (add if not present)

```groovy
android {
    signingConfigs {
        release {
            storeFile     file(localProperties['KEYSTORE_PATH'])
            storePassword localProperties['KEYSTORE_PASSWORD']
            keyAlias      localProperties['KEY_ALIAS']
            keyPassword   localProperties['KEY_PASSWORD']
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            // ...existing config
        }
    }
}
```

### 4. Build

```bash
# Signed APK
./gradlew assembleRelease

# Android App Bundle (recommended for Play Store)
./gradlew bundleRelease
```

Output locations:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

---

## Play Store Deployment

1. Open [Google Play Console](https://play.google.com/console)
2. Create a new app or select existing
3. Navigate to **Production → Create new release**
4. Upload the signed `.aab` file
5. Fill release notes and submit for review

**App details to prepare:**
- `versionCode` and `versionName` in `app/build.gradle` (currently `1` / `"1.0"`)
- App ID: `com.nexxlabs.chhotu`
- Required permissions declared in `AndroidManifest.xml`

---

## ProGuard / R8 Notes

- Rules are in `app/proguard-rules.pro`
- Gson serialisation of `StructuredIntent`, `ChatCompletionRequest/Response`, and `CommandHistoryItem` requires keep rules if not already annotated with `@SerializedName`
- Retrofit interfaces should be kept automatically by AGP; verify in the release build

---

## Environment Variables at Build Time

These must be set in `local.properties` before any release build:

| Variable | Description |
|---|---|
| `LLM_API_KEY` | API key injected as `BuildConfig.LLM_API_KEY` |
| `LLM_BASE_URL` | Base URL injected as `BuildConfig.LLM_BASE_URL` |

In a CI environment (GitHub Actions, Bitrise, etc.), these should be stored as encrypted secrets and written to `local.properties` as a build step:

```yaml
# Example GitHub Actions step
- name: Inject secrets
  run: |
    echo "LLM_API_KEY=${{ secrets.LLM_API_KEY }}" >> local.properties
    echo "LLM_BASE_URL=${{ secrets.LLM_BASE_URL }}" >> local.properties
```
