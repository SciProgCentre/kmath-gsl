job("Build") {
    gradlew("openjdk:11", "-Dorg.gradle.daemon=false", "build")
}
