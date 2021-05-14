# Module kmath-gsl

[KMath](https://github.com/mipt-npm/kmath) extension adding GNU Scientific Library based linear algebra implementation.

[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Gradle build](https://github.com/mipt-npm/kmath-gsl/workflows/build/badge.svg)](https://github.com/mipt-npm/kmath-gsl/actions/workflows/build.yml)
[![Space](https://img.shields.io/maven-metadata/v?label=Space&metadataUrl=https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven/space/kscience/kmath-gsl/maven-metadata.xml)](https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven/space/kscience/kmath-gsl/0.2.1-dev-1/)

 - [matrix-contexts](src/nativeMain/kotlin/GslLinearSpace.kt) : LinearSpace implementations for Double, Float, and Complex matrices and vectors implemented with GSL


## Artifact:

The Maven coordinates of this project are `space.kscience:kmath-gsl:0.3.0-dev-1`.

**Gradle:**
```gradle
repositories {
    mavenCentral()
    maven { url 'https://repo.kotlin.link' }
}

dependencies {
    implementation 'space.kscience:kmath-gsl:0.3.0-dev-1'
}
```
**Gradle Kotlin DSL:**
```kotlin
repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

dependencies {
    implementation("space.kscience:kmath-gsl:0.3.0-dev-1")
}
```
