pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.kotlin.link")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }

    plugins {
        id("ru.mipt.npm.gradle.project") version "0.10.2-fixrelease-1"
        id("de.undercouch.download") version "4.1.2"
        kotlin("multiplatform") version "1.5.30-RC"
    }
}

rootProject.name = "kmath-gsl"
