> #### Artifact:
>
> This module artifact: `${group}:${name}:${version}`.
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
>     implementation '${group}:${name}:${version}'
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
>     implementation("${group}:${name}:${version}")
> }
> ```