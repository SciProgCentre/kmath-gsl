job("Build") {
    container("mipt-npm.registry.jetbrains.space/p/sci/containers/ci-environment:1.0.1") {
        shellScript {
            interpreter = "/bin/bash"
            content = "./gradlew -Dorg.gradle.daemon=false build"
        }
    }
}
