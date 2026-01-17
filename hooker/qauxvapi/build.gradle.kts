plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.hooker.qauxvapi"
}

dependencies {
    implementation(projects.hooker.base)
    compileOnly(projects.stub.qauxvapi)
}
