plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.revanced.patcher)
    implementation(libs.smali)
}

tasks.jar {
    archiveBaseName = "${rootProject.name}-${project.name}"

    manifest {
        attributes["Name"] = "YABR ReVanced Patcher"
        attributes["Description"] = "load YABR"
        attributes["Version"] = version
        attributes["Timestamp"] = System.currentTimeMillis()
        attributes["Author"] = "o0kam1"
    }
}
