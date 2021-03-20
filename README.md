# GNU Scientific Library for KMath (`kmath-gsl`)

This [KMath](https://github.com/mipt-npm/kmath) extension implements the following features:

 - [matrix-contexts](src/nativeMain/kotlin/GslMatrixContext.kt) : Matrix Contexts over Double, Float, and Complex implemented with GSL


> #### Artifact:
>
> This module artifact: `space.kscience:kmath-gsl:0.2.1-dev-1`.
>
> **Gradle:**
>
> ```gradle
> repositories {
>     maven { url 'https://repo.kotlin.link' }
> }
> 
> dependencies {
>     implementation 'space.kscience:kmath-gsl:0.2.1-dev-1'
> }
> ```
> **Gradle Kotlin DSL:**
>
> ```kotlin
> repositories {
>     maven("https://repo.kotlin.link")
>     maven("https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven")
> }
> 
> dependencies {
>     implementation("space.kscience:kmath-gsl:0.2.1-dev-1")
> }
> ```

