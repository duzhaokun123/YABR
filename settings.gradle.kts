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
        maven {
            name = "Revanced Registry"
            url = uri("https://maven.pkg.github.com/revanced/registry")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_ACTOR"))
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GITHUB_TOKEN"))
            }
//            content {
//                includeGroup(" app.revanced")
//            }
        }
    }
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
include(":hooker:base")
include(":hooker:xposed")
include(":hooker:xposed100")
include(":hooker:pine")
include(":hooker:noop")
include(":revanced")