plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.stub.bilibili"
}

dependencies {
    api(libs.androidx.preference)
}
