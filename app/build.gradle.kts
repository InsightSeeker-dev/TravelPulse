plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "Projet.travelpulse"
    compileSdk = 35

    defaultConfig {
        applicationId = "Projet.travelpulse"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(project(":app:authactivity"))
    implementation(libs.play.services.location)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx) // Bien que ktx, peut être utilisé en Java
    implementation(libs.lifecycle.viewmodel.ktx) // Bien que ktx, peut être utilisé en Java
    implementation(libs.play.services.ads)
    implementation(libs.play.services.maps)

    // Suppression des dépendances Room (plus de stockage local)
    // implementation("androidx.room:room-runtime:2.6.1")
    // annotationProcessor("androidx.room:room-compiler:2.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Bloc de résolution supprimé car il causait une erreur de version inexistante