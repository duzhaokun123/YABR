plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.duzhaokun123.loader.libxposed"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {
    api(projects.loader.base)
    compileOnly(projects.core)
    compileOnly(libs.libxposed.api)
    compileOnly(projects.hooker.libxposed)
    compileOnly(libs.androidx.annotation)
}
