pluginManagement {
    resolutionStrategy {
        eachPlugin {
            // 干净环境可能无法解析 Paparazzi 插件标记，直接使用其 Maven Central 实现模块。
            if (requested.id.id == "app.cash.paparazzi") {
                useModule(
                    "app.cash.paparazzi:paparazzi-gradle-plugin:${requested.version}",
                )
            }
        }
    }
    repositories {
        maven {
            url = uri("https://repo1.maven.org/maven2")
            content {
                includeGroup("app.cash.paparazzi")
            }
        }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://repo1.maven.org/maven2")
            content {
                includeGroup("app.cash.paparazzi")
            }
        }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}

rootProject.name = "MyApp"
include(":app")
