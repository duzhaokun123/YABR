plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.loader.xposed"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(projects.loader.base)
    compileOnly(libs.xposed)
    compileOnly(projects.core)
    compileOnly(projects.hooker.xposed)
}