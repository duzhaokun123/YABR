plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.loader.xposed100"
}

dependencies {
    compileOnly(libs.libxposed.api)
    compileOnly(projects.app)
    compileOnly(projects.loader.base)
    compileOnly(projects.hooker.base)
    implementation(projects.hooker.xposed100)
}
