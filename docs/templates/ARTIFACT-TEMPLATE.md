## Artifact:

The Maven coordinates of this project are `${group}:${name}:${version}`.

**Gradle:**
```gradle
repositories {
    mavenCentral()
    maven { url 'https://repo.kotlin.link' }
}

dependencies {
    implementation '${group}:${name}:${version}'
}
```
**Gradle Kotlin DSL:**
```kotlin
repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

dependencies {
    implementation("${group}:${name}:${version}")
}
```