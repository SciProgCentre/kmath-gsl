# Module kmath-gsl

[![Gradle build](https://github.com/SciProgCentre/kmath-gsl/workflows/build/badge.svg)](https://github.com/SciProgCentre/kmath-gsl/actions/workflows/build.yml)
[![Space](https://img.shields.io/badge/dynamic/xml?color=orange&label=Space&query=//metadata/versioning/latest&url=https%3A%2F%2Fmaven.pkg.jetbrains.space%2Fspc%2Fp%2Fsci%2Fmaven%2Fspace%2Fkscience%2Fkmath-gsl%2Fmaven-metadata.xml)](https://maven.pkg.jetbrains.space/spc/p/sci/maven/space/kscience/)

[KMath](https://github.com/SciProgCentre/kmath) extension adding GNU Scientific Library based linear algebra implementation.

[Documentation site](https://sciprogcenter.github.io/kmath-gsl/)

 - [matrix-contexts](src/nativeMain/kotlin/GslLinearSpace.kt) : LinearSpace implementations for Double, Float, and Complex matrices and vectors implemented with GSL


## Artifact:

The Maven coordinates of this project are `space.kscience:kmath-gsl:0.3.0-dev-5`.

**Gradle Groovy:**
```groovy
repositories {
    maven { url 'https://repo.kotlin.link' }
    mavenCentral()
}

dependencies {
    implementation 'space.kscience:kmath-gsl:0.3.0-dev-5'
}
```
**Gradle Kotlin DSL:**
```kotlin
repositories {
    maven("https://repo.kotlin.link")
    mavenCentral()
}

dependencies {
    implementation("space.kscience:kmath-gsl:0.3.0-dev-5")
}
```

## Additional requirements

On Linux, final binaries using `kmath-gsl` need a BLAS implementation installed (because of dynamic linking
to `libblas`). To achieve better performance, MKL or ATLAS can be used.

On Windows, default CBLAS provided by GSL is linked statically. To build the kmath-gsl itself on Windows it is required
to have [MSYS2](https://www.msys2.org/) installed and added to Path (uncomment the `MSYS2_PATH_TYPE=inherit` line in
mingw64.ini) and GSL installed manually in the MSYS shell:

```shell
pacman --noconfirm -S mingw-w64-x86_64-gsl
```

## Multiplatform support

Currently, only `linuxX64` and `mingwX64` Kotlin targets are supported. It is also planned to support `macosX64`, `macosArm64`,
and even `jvm` (only after stable JDK release including [JEP-389](https://openjdk.java.net/jeps/389)).
