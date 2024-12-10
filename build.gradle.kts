// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion by extra("1.8.0") // Kotlin version
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.google.com") } // Add Maven repository explicitly
    }
    dependencies {
        // Gradle plugins
        classpath("com.android.tools.build:gradle:8.7.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.gms:google-services:4.3.15") // Firebase plugin
    }
}

plugins {
    // Plugin versions for modularized projects
    id("com.android.application") version "8.0.2" apply false
    id("com.android.library") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
