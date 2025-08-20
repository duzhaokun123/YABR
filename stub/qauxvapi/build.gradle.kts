plugins {
    id("com.android.library")
}

android {
    namespace = "lialh4.tencent.mqq.android.loader.qauxvapi"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    compileOnly(libs.androidx.annotation)
}
