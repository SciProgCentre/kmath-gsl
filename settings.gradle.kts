pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven("https://repo.kotlin.link")
        maven("https://dl.bintray.com/kotlin/kotlinx")
    }

    val toolsVersion = "0.8.1"

    plugins {
        id("kotlinx.benchmark") version "0.2.0-dev-20"
        id("ru.mipt.npm.gradle.project") version toolsVersion
        id("ru.mipt.npm.gradle.mpp") version toolsVersion
        id("ru.mipt.npm.gradle.publish") version toolsVersion
        id("de.undercouch.download") version "4.1.1"
    }
}

rootProject.name = "kmath-gsl"
