# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Kotlin & Coroutines ---
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod

# --- Hilt / Dagger ---
-keep class com.nexxlabs.chhotu.Hilt_** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class javax.annotation.** { *; }
-keep @dagger.hilt.EntryPoint class *
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.DefineComponent class *
-keep @javax.inject.Inject class * { *; }
-keep @javax.inject.Qualifier interface * { *; }
-keep @javax.inject.Scope interface * { *; }

# --- Retrofit & OkHttp ---
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep Retrofit interfaces
-keep interface com.nexxlabs.chhotu.data.remote.** { *; }

# --- Gson ---
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.nexxlabs.chhotu.data.remote.model.** { *; }

# --- Room ---
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.Room { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase

# --- Android Components ---
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# --- Domain Models (Start with generous keep for safety) ---
-keep class com.nexxlabs.chhotu.domain.registry.model.** { *; }

# --- Jetpack Compose ---
-keep class androidx.compose.** { *; }