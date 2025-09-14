import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

val defaultJavaJvmTarget by extra(JavaVersion.VERSION_11)
val defaultKotlinJvmTarget by extra(11)
val androidCompileSdkVersion by extra(36)
val androidTargetSdkVersion by extra(36)
val androidMinSdkVersion by extra(23)
val androidVersionCode by extra(1)
val androidVersionName by extra("1.0")

subprojects {
    plugins.withType<AndroidBasePlugin> {
        extensions.configure(CommonExtension::class) {
            compileSdk = androidCompileSdkVersion

            defaultConfig {
                minSdk = androidMinSdkVersion
                if (this is ApplicationDefaultConfig) {
                    targetSdk = androidTargetSdkVersion
                    versionCode = androidVersionCode
                    versionName = androidVersionName
                }
            }

            compileOptions {
                sourceCompatibility = defaultJavaJvmTarget
                targetCompatibility = defaultJavaJvmTarget
            }
        }
    }
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = defaultJavaJvmTarget
            targetCompatibility = defaultJavaJvmTarget
        }
    }
    plugins.withType<KotlinBasePlugin> {
        runCatching {
            extensions.configure<KotlinAndroidProjectExtension> {
                jvmToolchain(defaultKotlinJvmTarget)
            }
        }
        runCatching {
            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(defaultKotlinJvmTarget)
            }
        }
    }
}