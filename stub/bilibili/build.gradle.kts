plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.stub.bilibili"
}

dependencies {
    api(libs.androidx.preference)
}
