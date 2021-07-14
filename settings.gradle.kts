pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.kotlin.link")
    }

    plugins {
        id("ru.mipt.npm.gradle.project") version "0.10.5"
        id("de.undercouch.download") version "4.1.2"
        kotlin("multiplatform") version "1.6.0-RC"
        id("io.github.krakowski.jextract") version "0.2.1"
    }
}

rootProject.name = "kmath-gsl"
