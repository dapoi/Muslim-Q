import com.dapascript.buildsrc.Libs
import com.dapascript.buildsrc.SharedLibs

plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.prodev.muslimq.core"
    compileSdk = 34

    defaultConfig {
        minSdk = Libs.minSdk
        targetSdk = Libs.targetSdk

        testInstrumentationRunner = Libs.testInstrumentationRunner
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
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

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Room
    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.6")

    // Moshi
    api("com.squareup.moshi:moshi-kotlin:1.14.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")

    // Coroutines
    val coroutinesVersion = "1.7.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Coroutine lifecycle scopes
    val lifeCycleScopeVersion = "2.6.2"
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleScopeVersion")
    api("androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycleScopeVersion")
    api("androidx.lifecycle:lifecycle-livedata-ktx:$lifeCycleScopeVersion")

    // Location
    api("com.google.android.gms:play-services-location:21.0.1")
}