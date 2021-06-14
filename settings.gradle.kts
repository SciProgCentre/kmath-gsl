pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.kotlin.link")
    }

    val toolsVersion = "0.9.9"

    plugins {
        id("ru.mipt.npm.gradle.project") version toolsVersion
        id("ru.mipt.npm.gradle.common") version toolsVersion
        id("de.undercouch.download") version "4.1.1"
        kotlin("multiplatform") version "1.5.10"
    }
}

rootProject.name = "kmath-gsl"
