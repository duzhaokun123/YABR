plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "io.github.duzhaokun123.yabr"

    defaultConfig {
        applicationId = "io.github.duzhaokun123.yabr"
        buildConfigField("Long", "BUILD_TIME", "${System.currentTimeMillis()}L")
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

    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x25")
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(kotlin("reflect"))

    implementation(projects.loader.base)
    runtimeOnly(projects.loader.xposed)
    runtimeOnly(projects.loader.xposed100)
    runtimeOnly(projects.loader.inline)
    runtimeOnly(projects.loader.acf)
    runtimeOnly(projects.loader.rxposed)

    implementation(projects.hooker.base)
    runtimeOnly(projects.hooker.noop)
    runtimeOnly(projects.hooker.pine)
    runtimeOnly(projects.hooker.xposed)
    runtimeOnly(projects.hooker.xposed100)

    compileOnly(projects.stub.bilibili)

    ksp(projects.annotation)

    implementation(libs.dexkit)
    implementation(libs.photoview)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.unsafe)
}