// change to kts
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.0")
        classpath("com.google.gms:google-services:4.4.3")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.4")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.1")
    }
}

plugins {
    id("com.android.application") version "8.13.0" apply false
    id("com.android.library") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.0" apply false
}