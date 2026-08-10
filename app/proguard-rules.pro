# === 项目 keep 规则（精简版：依赖自带 consumer rules，不再整包保留） ===

# Room 实体（反射访问字段）
-keep class com.babytracker.core.database.entity.** { *; }

# Koin（反射实例化 ViewModel / Repository）
-keep class com.babytracker.** { <init>(...); }
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Retrofit / OkHttp / Okio：保留接口注解与泛型签名，实现类交给库自带 consumer rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Gson（Retrofit converter）：只需保留类型令牌与 Unsafe
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class sun.misc.Unsafe { *; }
-dontwarn com.google.gson.**

# Compose / Coil：自带 consumer rules，不再整包 keep
-dontwarn androidx.compose.**
-dontwarn coil.**

# kotlinx.serialization（Room/Supabase/AI 配置序列化）
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keep,includedescriptorclasses class com.babytracker.**$$serializer { *; }
-keepclassmembers,allowshrinking,allowobfuscation class * {
    @kotlinx.serialization.Serializable <fields>;
}

# R 类
-keep class **.R { *; }
-keep class **.R$* { *; }
