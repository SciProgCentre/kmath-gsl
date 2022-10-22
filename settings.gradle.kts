pluginManagement.repositories {
    maven("https://repo.kotlin.link")
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

dependencyResolutionManagement {
    repositories {
        maven("https://repo.kotlin.link")
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }

    versionCatalogs.create("kscienceLibs") {
        from("space.kscience:version-catalog:0.13.1-kotlin-1.7.20")
    }
}

rootProject.name = "kmath-gsl"
