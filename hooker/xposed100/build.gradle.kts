plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.hooker.xposed100"
}

dependencies {
    compileOnly(libs.libxposed.api)
    implementation(projects.hooker.base)
}
