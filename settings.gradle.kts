pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.kotlin.link")
    }

    val toolsVersion = "0.10.0"

    plugins {
        id("ru.mipt.npm.gradle.project") version toolsVersion
        id("ru.mipt.npm.gradle.common") version toolsVersion
        id("de.undercouch.download") version "4.1.2"
        kotlin("multiplatform") version "1.5.21"
    }
}

rootProject.name = "kmath-gsl"
