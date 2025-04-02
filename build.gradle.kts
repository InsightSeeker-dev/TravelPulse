// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Ajoutez le classpath pour Google Services
        classpath(libs.google.services)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}