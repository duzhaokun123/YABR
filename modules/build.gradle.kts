plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.o0kam1.modules"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

ksp {
    arg("classname", "FeatureModuleEntries")
    arg("activitiesClassname", "FeatureModuleActivities")
}

dependencies {
    implementation(projects.core)
    implementation(projects.hooker.base)
    implementation(projects.loader.base)

    compileOnly(projects.stub.bilibili)

    ksp(projects.annotation)

    implementation(kotlin("reflect"))
    implementation(libs.dexkit)
    implementation(libs.photoview)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlinx.serialization.json)
}
