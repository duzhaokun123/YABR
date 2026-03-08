plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutLibraries.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.github.duzhaokun123.yabr.core"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("Long", "BUILD_TIME", "${System.currentTimeMillis()}L")
        buildConfigField("String", "VERSION_NAME", "\"${project.rootProject.extra["androidVersionName"]}\"")
        buildConfigField("int", "VERSION_CODE", "${project.rootProject.extra["androidVersionCode"]}")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

ksp {
    arg("classname", "CoreModuleEntries")
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(kotlin("reflect"))

    implementation(projects.loader.base)
    implementation(projects.hooker.base)

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
