pluginManagement {
    resolutionStrategy {
        eachPlugin {
            // Paparazzi 未发布 Plugin DSL 标记，需直接解析 Maven Central 中的官方实现模块。
            if (requested.id.id == "app.cash.paparazzi") {
                useModule(
                    "app.cash.paparazzi:paparazzi-gradle-plugin:${requested.version}",
                )
            }
        }
    }
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
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
