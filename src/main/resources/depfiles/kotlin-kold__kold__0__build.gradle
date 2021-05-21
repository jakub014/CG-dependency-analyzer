buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    java
    id("java-library")
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.4.20"
}

allprojects {
    version = "0.2"
    repositories {
        mavenLocal()
        mavenCentral()
    }

    group = "com.github.kotlin-kold"
}
