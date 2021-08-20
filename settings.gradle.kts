pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.kotlin.link")
    }

    plugins {
        id("ru.mipt.npm.gradle.project") version "0.10.2-fixrelease-1"
        id("de.undercouch.download") version "4.1.2"
        kotlin("multiplatform") version "1.5.21"
    }
}

rootProject.name = "kmath-gsl"
