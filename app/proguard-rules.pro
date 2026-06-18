# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# --- Koin ---
-keep class org.koin.** { *; }
-keep class com.babytracker.core.di.** { *; }

# --- Retrofit / OkHttp ---
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# --- JSON ---
-keep class org.json.** { *; }

# --- App entities (Room / Gson / reflection) ---
-keep class com.babytracker.core.database.entity.** { *; }
-keep class com.babytracker.core.backup.** { *; }

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
