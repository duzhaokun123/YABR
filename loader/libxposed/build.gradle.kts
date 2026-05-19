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
    compileOnly(libs.libxposed.api)
    compileOnly(projects.core)
    compileOnly(projects.loader.base)
    compileOnly(projects.hooker.base)
    compileOnly(projects.hooker.libxposed)
    implementation(libs.androidx.annotation)
}
