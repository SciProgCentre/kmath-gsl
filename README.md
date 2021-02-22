# GNU Scientific Library for KMath (`kmath-gsl`)

This subproject implements the following features:

 - [matrix-contexts](src/nativeMain/kotlin/GslMatrixContext.kt) : Matrix Contexts over Double, Float, and Complex implemented with GSL


> #### Artifact:
>
> This module artifact: `space.kscience:kmath-gsl:0.2.0-dev-1`.
>
> **Gradle:**
>
> ```gradle
> repositories {
>     maven { url 'https://dl.bintray.com/hotkeytlt/maven' }
>     maven { url 'https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven' }
> }
> 
> dependencies {
>     implementation 'space.kscience:kmath-gsl:0.2.0-dev-1'
> }
> ```
> **Gradle Kotlin DSL:**
>
> ```kotlin
> repositories {
>     maven("https://dl.bintray.com/hotkeytlt/maven")
>     maven("https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven")
> }
> 
> dependencies {
>     implementation("space.kscience:kmath-gsl:0.2.0-dev-1")
> }
> ```
