plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.hooker.noop"
}

dependencies {
    implementation(projects.hooker.base)
}
