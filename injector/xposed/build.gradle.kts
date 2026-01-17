plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.duzhaokun123.yabr.injector.xposed"
}

dependencies {
    compileOnly(libs.xposed)
}