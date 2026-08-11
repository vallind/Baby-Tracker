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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.babytracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 23
        versionName = "1.8.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin { compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }

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

    // Navigation
    implementation(libs.navigation.compose)

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

// —— 令牌审计门禁：themeTokenAudit（共享 TokenAuditChecker，双路复用）——
// debugCompileClasspath 带 Android 变体属性，直接引用其解析结果可避免拖入缺失缓存的 JVM 变体依赖
tasks.register<JavaExec>("themeTokenAudit") {
    group = "verification"
    description = "令牌化静态审计：扫描全部 Kotlin 源码，拦截 M3 令牌直用/硬编码颜色/令牌绕过"
    dependsOn("compileDebugKotlin")
    // AGP 9 无 sourceSets 容器，主类输出取 compileDebugKotlin 的目标目录（本项目无 Java 源码）
    val kotlinClasses = tasks.named("compileDebugKotlin")
        .flatMap { (it as org.jetbrains.kotlin.gradle.tasks.KotlinCompile).destinationDirectory }
        .map { it.asFile }
    classpath = configurations.getByName("debugCompileClasspath") + files(kotlinClasses)
    mainClass = "com.babytracker.core.util.TokenAuditCheckerKt"
    args(
        project.file("src/main/java").absolutePath,
        "com/babytracker/designsystem/theme",
        "com/babytracker/designsystem/components",
        project.file("src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt").absolutePath,
    )
}
