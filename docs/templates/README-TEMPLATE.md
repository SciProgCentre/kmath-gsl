# Module kmath-gsl

[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Gradle build](https://github.com/mipt-npm/kmath-gsl/workflows/build/badge.svg)](https://github.com/mipt-npm/kmath-gsl/actions/workflows/build.yml)
[![Space](https://img.shields.io/badge/dynamic/xml?color=orange&label=Space&query=//metadata/versioning/latest&url=https%3A%2F%2Fmaven.pkg.jetbrains.space%2Fmipt-npm%2Fp%2Fsci%2Fmaven%2Fspace%2Fkscience%2Fkmath-gsl%2Fmaven-metadata.xml)](https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven/space/kscience/)

[KMath](https://github.com/mipt-npm/kmath) extension adding GNU Scientific Library based linear algebra implementation.

[Documentation site](https://mipt-npm.github.io/kmath-gsl/)

${features}

${artifact}

## Additional requirements

Final binaries using `kmath-gsl` need a BLAS implementation installed (because of dynamic linking to `libblas`). To achieve better performance, MKL or ATLAS can be used.

## Multiplatform support

Currently, only `linuxX64` is supported. Planned platforms are `macosX64`, `mingwX64`, and `jvm` (with [JEP-389](https://openjdk.java.net/jeps/389)).
