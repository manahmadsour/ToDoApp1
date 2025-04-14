// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false

}
tasks.register("ijDownloadSources") {
    doLast {
        println("Downloading sources...")
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val kotlin_version = "2.1.0"
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath ("com.google.gms:google-services:4.3.15")



    }

}

