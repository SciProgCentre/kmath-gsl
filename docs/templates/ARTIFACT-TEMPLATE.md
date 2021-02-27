> #### Artifact:
>
> This module artifact: `${group}:${name}:${version}`.
>
> **Gradle:**
>
> ```gradle
> repositories {
>     maven { url 'https://repo.kotlin.link' }
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
>     maven("https://repo.kotlin.link")
>     maven("https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven")
> }
> 
> dependencies {
>     implementation("${group}:${name}:${version}")
> }
> ```
