plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutLibraries.android)
    alias(libs.plugins.compose.compiler)
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
        compose = true
    }

    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x25")
    }

//    packaging {
//        resources {
//            excludes += "**"
//        }
//    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(kotlin("reflect"))

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

    compileOnly(projects.stub.bilibili)

    ksp(projects.annotation)

    implementation(libs.dexkit)
    implementation(libs.photoview)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.unsafe)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.activity)
}