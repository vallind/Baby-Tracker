pluginManagement {
    repositories {
        // aliyun 镜像对 KSP 新版 marker 返回 502，本地直连官方源
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}

rootProject.name = "MyApp"
include(":app")
include(":detekt-rules")
