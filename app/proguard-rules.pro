# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Room entities
-keep class com.babytracker.core.database.entity.** { *; }

# Keep Koin
-keep class org.koin.** { *; }

# Keep Retrofit/OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep JSON serialization
-keep class org.json.** { *; }
