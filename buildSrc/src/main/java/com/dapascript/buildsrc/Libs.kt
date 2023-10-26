package com.dapascript.buildsrc

import org.gradle.api.artifacts.dsl.DependencyHandler

object Libs {

    const val appId = "com.prodev.muslimq"
    const val minSdk = 24
    const val targetSdk = 34
    const val versionCode = 20231018
    const val versionName = "2.7.1"
    const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    private fun DependencyHandler.implementation(dependency: Any) {
        add("implementation", dependency)
    }

    private fun DependencyHandler.kapt(dependency: Any) {
        add("kapt", dependency)
    }

    private fun DependencyHandler.debugImplementation(dependency: Any) {
        add("debugImplementation", dependency)
    }

    private fun DependencyHandler.releaseImplementation(dependency: Any) {
        add("releaseImplementation", dependency)
    }

    fun applySharedDeps(dependencyHandler: DependencyHandler) {
        dependencyHandler.apply {
            // Main
            implementation("androidx.core:core-ktx:1.12.0")
            implementation("androidx.appcompat:appcompat:1.6.1")
            implementation("com.google.android.material:material:1.10.0")
            implementation("androidx.constraintlayout:constraintlayout:2.1.4")
            implementation("androidx.datastore:datastore-preferences:1.0.0")

            // Testing
            implementation("junit:junit:4.13.2")
            implementation("androidx.test.ext:junit:1.1.5")
            implementation("androidx.test.espresso:espresso-core:3.5.1")

            // Appearance
            implementation("com.github.bumptech.glide:glide:4.14.2")
            implementation("com.airbnb.android:lottie:5.2.0")
            implementation("com.facebook.shimmer:shimmer:0.5.0")
            implementation("com.github.SimformSolutionsPvtLtd:SSPullToRefresh:1.3")
            implementation("com.tbuonomo:dotsindicator:4.2")
            implementation("com.aminography:primecalendar:1.7.0")

            // Navigation
            val navigationVersion = "2.5.3"
            implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
            implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

            // Dagger hilt
            val daggerHiltVersion = "2.48.1"
            implementation("com.google.dagger:hilt-android:$daggerHiltVersion")
            kapt("com.google.dagger:hilt-android-compiler:$daggerHiltVersion")

            // Chuck library
            val chuckVersion = "3.5.2"
            debugImplementation("com.github.chuckerteam.chucker:library:$chuckVersion")
            releaseImplementation("com.github.chuckerteam.chucker:library-no-op:$chuckVersion")

            // Firebase
            implementation("com.google.firebase:firebase-crashlytics-ktx:18.4.3")

            // API Splash Screen
            implementation("androidx.core:core-splashscreen:1.0.1")

            // ShowCaseView
            implementation("uk.co.samuelwall:material-tap-target-prompt:3.3.2")
        }
    }
}