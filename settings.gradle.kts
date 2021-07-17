pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.kotlin.link")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }

    plugins {
        id("ru.mipt.npm.gradle.project") version "0.10.1"
        id("de.undercouch.download") version "4.1.2"
        kotlin("multiplatform") version "1.5.30-RC-161"
    }
}

rootProject.name = "kmath-gsl"
