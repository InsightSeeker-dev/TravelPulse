// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Ajoutez le classpath pour Google Services
        // classpath(libs.google.services)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}