job("Build") {
    container("mipt-npm.registry.jetbrains.space/p/sci/containers/ci-environment:1.0.0") {
        shellScript {
            interpreter = "/bin/bash"

            content = """
                apt update
                apt install -y libncurses5
                ./gradlew -Dorg.gradle.daemon=false build
            """.trimIndent()
        }
    }
}
