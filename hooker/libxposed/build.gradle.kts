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
    api(projects.hooker.base)
    compileOnly(libs.libxposed.api)
    compileOnly(libs.androidx.annotation)
}
