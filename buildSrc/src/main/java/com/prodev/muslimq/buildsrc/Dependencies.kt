package com.prodev.muslimq.buildsrc

object Lib {
    // Main
    const val coreKtx = "androidx.core:core-ktx:1.10.1"
    const val appcompat = "androidx.appcompat:appcompat:1.6.1"
    const val material = "com.google.android.material:material:1.9.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
    const val datastorePreferences = "androidx.datastore:datastore-preferences:1.0.0"

    // Testing
    const val junit = "junit:junit:4.13.2"
    const val junitExt = "androidx.test.ext:junit:1.1.5"
    const val espressoCore = "androidx.test.espresso:espresso-core:3.5.1"

    // Appearance
    const val glide = "com.github.bumptech.glide:glide:4.14.2"
    const val lottie = "com.airbnb.android:lottie:5.2.0"
    const val shimmer = "com.facebook.shimmer:shimmer:0.5.0"
    const val ssPullToRefresh = "com.github.SimformSolutionsPvtLtd:SSPullToRefresh:1.3"
    const val dotsIndicator = "com.tbuonomo:dotsindicator:4.2"
    const val primeCalendar = "com.apachat:primecalendar-android:1.3.93"

    // Navigation
    private const val navigationVersion = "2.5.3"
    const val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:$navigationVersion"

    // Dagger Hilt
    private const val daggerHiltVersion = "2.44"
    const val hiltAndroid = "com.google.dagger:hilt-android:$daggerHiltVersion"
    const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:$daggerHiltVersion"

    // Chuck library
    private const val chuckVersion = "3.5.2"
    const val chuckerLibraryDebug = "com.github.chuckerteam.chucker:library:$chuckVersion"
    const val chuckerLibraryRelease = "com.github.chuckerteam.chucker:library-no-op:$chuckVersion"

    // Firebase
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-ktx:18.4.0"

    // API Splash Screen
    const val coreSplashScreen = "androidx.core:core-splashscreen:1.0.1"

    // ShowCaseView
    const val materialTapTargetPrompt = "uk.co.samuelwall:material-tap-target-prompt:3.3.2"
}
