import com.dapascript.buildsrc.Libs

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.prodev.muslimq"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file("D:\\Projek\\Key\\muslimkey.jks")
            storePassword = "Lutpi220201"
            keyAlias = "muslimq"
            keyPassword = "Lutpi220201"
        }
    }

    defaultConfig {
        applicationId = Libs.appId
        minSdk = Libs.minSdk
        targetSdk = Libs.targetSdk
        versionCode = Libs.versionCode
        versionName = Libs.versionName

        testInstrumentationRunner = Libs.testInstrumentationRunner
        vectorDrawables.useSupportLibrary = true
        signingConfig = signingConfigs.getByName("release")
    }

    android {
        ndkVersion = "21.4.7075529"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {
    // Apply shared dependencies
    Libs.applySharedDeps(dependencies)

    // Core
    implementation(project(":core"))
}