package com.dapascript.buildsrc

import org.gradle.api.artifacts.dsl.DependencyHandler

object Libs {

    // App
    const val appId = "com.prodev.muslimq"
    const val minSdk = 26
    const val targetSdk = 36
    const val versionCode = 20250709
    const val versionName = "3.3"
    const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Main
    const val coreKtx = "androidx.core:core-ktx:1.16.0"
    const val appCompat = "androidx.appcompat:appcompat:1.7.1"
    const val material = "com.google.android.material:material:1.12.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.2.1"
    const val datastore = "androidx.datastore:datastore-preferences:1.1.4"

    // Appearance
    const val glide = "com.github.bumptech.glide:glide:4.14.2"
    const val lottie = "com.airbnb.android:lottie:5.2.0"
    const val shimmer = "com.facebook.shimmer:shimmer:0.5.0"
    const val pullToRefresh = "com.github.SimformSolutionsPvtLtd:SSPullToRefresh:1.3"
    const val dotsIndicator = "com.tbuonomo:dotsindicator:4.2"
    const val primeCalendar = "com.aminography:primecalendar:1.7.0"

    // Navigation
    private const val navigationVersion = "2.8.9"
    const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
    const val navigationUi = "androidx.navigation:navigation-ui-ktx:$navigationVersion"

    // Api Splash Screen
    const val apiSplashScreen = "androidx.core:core-splashscreen:1.0.1"

    // ShowCaseView
    const val showCaseView = "uk.co.samuelwall:material-tap-target-prompt:3.3.2"
}

object SharedLibs {

    private fun DependencyHandler.implementation(dependency: Any) {
        add("implementation", dependency)
    }

    private fun DependencyHandler.ksp(dependency: Any) {
        add("ksp", dependency)
    }

    private fun DependencyHandler.debugImplementation(dependency: Any) {
        add("debugImplementation", dependency)
    }

    private fun DependencyHandler.releaseImplementation(dependency: Any) {
        add("releaseImplementation", dependency)
    }

    fun applySharedDeps(dependencyHandler: DependencyHandler) {
        dependencyHandler.apply {
            // Testing
            implementation("junit:junit:4.13.2")
            implementation("androidx.test.ext:junit:1.1.5")
            implementation("androidx.test.espresso:espresso-core:3.5.1")

            // Dagger hilt
            val daggerHiltVersion = "2.53.1"
            implementation("com.google.dagger:hilt-android:$daggerHiltVersion")
            ksp("com.google.dagger:hilt-android-compiler:$daggerHiltVersion")

            // Chuck library
            val chuckVersion = "3.5.2"
            debugImplementation("com.github.chuckerteam.chucker:library:$chuckVersion")
            releaseImplementation("com.github.chuckerteam.chucker:library-no-op:$chuckVersion")

            // Firebase
            implementation("com.google.firebase:firebase-crashlytics-ktx:18.4.3")
        }
    }
}