plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.loader.inline"
}

dependencies {
    compileOnly(projects.app)
    compileOnly(projects.loader.base)
    compileOnly(projects.hooker.base)
    compileOnly(projects.hooker.pine)
    compileOnly(projects.hooker.noop)
}
