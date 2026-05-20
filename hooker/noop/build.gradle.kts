plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.hooker.noop"
}

dependencies {
    api(projects.hooker.base)
}
