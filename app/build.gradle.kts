plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.duzhaokun123.yabr"

    defaultConfig {
        applicationId = "io.github.duzhaokun123.yabr"
        buildConfigField("Long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    }

    signingConfigs {
        getByName("debug") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
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

    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x25")
    }
}

dependencies {
    implementation(projects.core)
    implementation(projects.modules)

    implementation(projects.loader.base)
    runtimeOnly(projects.loader.xposed)
//    runtimeOnly(projects.loader.xposed100)
//    runtimeOnly(projects.loader.inline)
//    runtimeOnly(projects.loader.acf)
//    runtimeOnly(projects.loader.rxposed)
//    runtimeOnly(projects.loader.qauxvapi)

    implementation(projects.hooker.base)
//    runtimeOnly(projects.hooker.noop)
//    runtimeOnly(projects.hooker.pine)
    runtimeOnly(projects.hooker.xposed)
//    runtimeOnly(projects.hooker.xposed100)
//    runtimeOnly(projects.hooker.qauxvapi)
}