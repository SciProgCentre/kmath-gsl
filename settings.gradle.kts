enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

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

    versionCatalogs.create("miptNpm") {
        from("ru.mipt.npm:version-catalog:0.10.7")
    }
}

rootProject.name = "kmath-gsl"
