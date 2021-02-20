job("Build") {
    container("openjdk:11.0.10-jdk-buster") {
        shellScript {
            interpreter = "/bin/bash"

            content = """
                apt install libncurses5
                ./gradlew -Dorg.gradle.daemon=false build
            """.trimIndent()
        }
    }
}
