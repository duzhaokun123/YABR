plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.hooker.xposed"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(projects.hooker.base)
    compileOnly(libs.xposed)
}