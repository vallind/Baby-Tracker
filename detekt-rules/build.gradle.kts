plugins {
    // KGP 已由其他插件带入 build classpath（版本不可直接校验），
    // 此处不带版本号请求，直接复用 classpath 上的实例
    kotlin("jvm")
}

kotlin {
    // Termux 无 JDK17 工具链，直接声明字节码目标，复用运行中的 JDK21 编译
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // compileOnly：detekt 运行时自带 detekt-api，避免规则 jar 重复打包
    compileOnly(libs.detekt.api)
}
