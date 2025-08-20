plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.duzhaokun123.loader.qauxvapi"
}

dependencies {
    compileOnly(projects.app)
    compileOnly(projects.loader.base)
    compileOnly(projects.hooker.base)
    compileOnly(projects.stub.qauxvapi)
    compileOnly(projects.hooker.qauxvapi)
}
