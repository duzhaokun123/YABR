plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.hooker.xposed100"
}

dependencies {
    compileOnly(libs.libxposed.api)
    implementation(projects.hooker.base)
}
