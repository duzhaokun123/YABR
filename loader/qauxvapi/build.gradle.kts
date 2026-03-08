plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.loader.qauxvapi"
}

dependencies {
    compileOnly(projects.core)
    compileOnly(projects.loader.base)
    compileOnly(projects.hooker.base)
    compileOnly(projects.stub.qauxvapi)
    compileOnly(projects.hooker.qauxvapi)
}
