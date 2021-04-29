pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.kotlin.link")
    }

    val toolsVersion = "0.9.5-dev-2"

    plugins {
        id("kotlinx.benchmark") version "0.30"
        id("ru.mipt.npm.gradle.project") version toolsVersion
        id("ru.mipt.npm.gradle.common") version toolsVersion
        id("de.undercouch.download") version "4.1.1"
        kotlin("multiplatform") version "1.4.32"
    }
}

rootProject.name = "kmath-gsl"
