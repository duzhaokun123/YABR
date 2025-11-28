enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://api.xposed.info") {
            content {
                includeGroup("de.robv.android.xposed")
            }
        }
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.iamironz")
            }
        }
        mavenLocal {
            content {
                includeGroup("io.github.libxposed")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "YABR"

include(":app")
include(":annotation")

include(":loader:base")
include(":loader:xposed")
include(":loader:xposed100")
include(":loader:inline")
include(":loader:acf")
include(":loader:rxposed")
include(":loader:qauxvapi")

include(":hooker:base")
include(":hooker:xposed")
include(":hooker:xposed100")
include(":hooker:pine")
include(":hooker:noop")
include(":hooker:qauxvapi")

include(":injector:zygisk")
include(":injector:xposed")

include(":stub:bilibili")
include(":stub:qauxvapi")
