pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://dl.bintray.com/mipt-npm/kscience")
        maven("https://dl.bintray.com/mipt-npm/dev")
        maven("https://dl.bintray.com/kotlin/kotlinx")
    }

    val toolsVersion = "0.7.6"

    plugins {
        id("kotlinx.benchmark") version "0.2.0-dev-20"
        id("ru.mipt.npm.mpp") version toolsVersion
        id("ru.mipt.npm.project") version toolsVersion
        id("ru.mipt.npm.publish") version toolsVersion
        id("de.undercouch.download") version "4.1.1"
    }
}

rootProject.name = "kmath-gsl"
