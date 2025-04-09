import com.dapascript.buildsrc.Libs
import com.dapascript.buildsrc.SharedLibs

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.prodev.muslimq"
    compileSdk = Libs.targetSdk

    signingConfigs {
        create("release") {
            storeFile = file("C:\\Projek\\muslimkey.jks")
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
    SharedLibs.applySharedDeps(dependencies)

    // Core
    implementation(project(":core"))

    // Main
    implementation(Libs.coreKtx)
    implementation(Libs.appCompat)
    implementation(Libs.material)
    implementation(Libs.constraintLayout)
    implementation(Libs.datastore)

    // Appearance
    implementation(Libs.glide)
    implementation(Libs.lottie)
    implementation(Libs.shimmer)
    implementation(Libs.pullToRefresh)
    implementation(Libs.dotsIndicator)
    implementation(Libs.primeCalendar)

    // Navigation
    implementation(Libs.navigationFragment)
    implementation(Libs.navigationUi)

    // Api Splash Screen
    implementation(Libs.apiSplashScreen)

    // ShowCaseView
    implementation(Libs.showCaseView)
}