plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.yabr.injector.xposed"
}

dependencies {
    compileOnly(libs.xposed)
}