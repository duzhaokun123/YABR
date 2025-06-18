plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.hooker.noop"
}

dependencies {
    implementation(projects.hooker.base)
}
