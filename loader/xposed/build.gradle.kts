plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.loader.xposed"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    compileOnly(libs.xposed)
    compileOnly(projects.app)
    compileOnly(projects.loader.base)
    compileOnly(projects.hooker.base)
    compileOnly(projects.hooker.xposed)
}