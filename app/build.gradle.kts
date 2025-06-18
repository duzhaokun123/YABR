import org.gradle.kotlin.dsl.debug

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "io.github.duzhaokun123.yabr"

    defaultConfig {
        applicationId = "io.github.duzhaokun123.yabr"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.annotation)

    implementation(projects.loader.base)
    runtimeOnly(projects.loader.xposed)
    runtimeOnly(projects.loader.xposed100)
    runtimeOnly(projects.loader.inline)
    runtimeOnly(projects.loader.acf)
    runtimeOnly(projects.loader.rxposed)

    implementation(projects.hooker.base)

    ksp(projects.annotation)

    implementation(libs.dexkit)
}