plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.loader.inline"
}

dependencies {
    compileOnly(projects.core)
    compileOnly(projects.loader.base)
    compileOnly(projects.hooker.base)
    compileOnly(projects.hooker.pine)
    compileOnly(projects.hooker.noop)
}
