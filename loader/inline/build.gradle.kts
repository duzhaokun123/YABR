plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.loader.inline"
}

dependencies {
    api(projects.loader.base)
    compileOnly(projects.core)
    compileOnly(projects.hooker.pine)
    compileOnly(projects.hooker.noop)
}
