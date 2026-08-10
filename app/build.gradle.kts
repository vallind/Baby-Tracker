plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.babytracker"
    // Elyon 及其传递依赖（lifecycle 2.11 / material-color-utilities 5.0）要求 37
    compileSdk = 37

    defaultConfig {
        applicationId = "com.babytracker"
        // elyon-blur 要求 Android 13+（minSdk 33），全面启用毛玻璃效果必须同步提升
        minSdk = 33
        targetSdk = 36
        versionCode = 22
        versionName = "1.7.11"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        // elyon-nav 的 inline 函数以 JVM target 21 编译，应用必须对齐才能内联
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin { compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) }

    buildFeatures { compose = true }

}

dependencies {
    // Core
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.datastore)
    implementation(libs.serialization.json)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)

    // Elyon UI 基座（vide/elegant 复合构建）
    implementation(project(":elegant:elyon-core"))
    implementation(project(":elegant:elyon-ui"))
    implementation(project(":elegant:elyon-effects"))
    implementation(project(":elegant:elyon-blur"))
    implementation(project(":elegant:elyon-nav"))

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.okhttp)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coil
    implementation(libs.coil.compose)

    // Vico
    implementation(libs.vico.compose.m3)

// DocumentFile
implementation(libs.documentfile)

// Logging
implementation(libs.timber)
    implementation(libs.commonmark)
    implementation(libs.commonmark.strikethrough)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Lifecycle Process
    implementation(libs.lifecycle.process)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.paparazzi)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Detekt 自定义规则（HardcodedColor/TokenBypass，ServiceLoader 注册）
    detektPlugins(project(":detekt-rules"))
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// —— Detekt 静态分析（配置见 config/detekt/detekt.yml）——
detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    // detekt 门禁（report-only，不阻断）：存量债务登记见 docs/design-system.md 与 CHANGELOG
    // 不用 buildUponDefaultConfig：全量默认规则在 Termux 上分析过慢（>20 分钟），
    // 改用显式枚举规则（见 config/detekt/detekt.yml 注释，实测 ~9 秒）
    ignoreFailures = true
}

