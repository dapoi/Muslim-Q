plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}
apply(from = "../shared_dependencies.gradle")

android {
    namespace = "com.prodev.muslimq"
    compileSdk = 33

    signingConfigs {
        create("release") {
            storeFile = file("D:\\Projek\\Key\\muslimkey.jks")
            storePassword = "Lutpi220201"
            keyAlias = "muslimq"
            keyPassword = "Lutpi220201"
        }
    }

    defaultConfig {
        applicationId = "com.prodev.muslimq"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        signingConfig = signingConfigs.getByName("release")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    ndkVersion = "21.4.7075529"
}

dependencies {
    // Core
    implementation(project(":core"))
}