plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.loader.rxposed"
}

dependencies {
    api(projects.loader.base)
    compileOnly(projects.core)
    compileOnly(projects.hooker.pine)
}
