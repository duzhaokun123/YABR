plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.hooker.pine"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.hooker.base)
    implementation(libs.pine.core)
    compileOnly(projects.app)
}
