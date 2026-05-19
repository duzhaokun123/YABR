plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.hooker.libxposed"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    compileOnly(libs.libxposed.api)
    implementation(projects.hooker.base)
    implementation(libs.androidx.annotation)
}
