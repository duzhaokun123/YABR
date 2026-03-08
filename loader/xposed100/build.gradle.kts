plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.loader.xposed100"
}

dependencies {
    compileOnly(libs.libxposed.api)
    compileOnly(projects.core)
    compileOnly(projects.loader.base)
    compileOnly(projects.hooker.base)
    compileOnly(projects.hooker.xposed100)
}
