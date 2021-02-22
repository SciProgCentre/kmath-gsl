@file:Suppress("UNUSED_VARIABLE")

import de.undercouch.gradle.tasks.download.Download
import space.kscience.kmath.gsl.codegen.matricesCodegen
import space.kscience.kmath.gsl.codegen.vectorsCodegen
import ru.mipt.npm.gradle.Maturity

plugins {
    id("ru.mipt.npm.gradle.project")
    id("ru.mipt.npm.gradle.mpp")
    id("ru.mipt.npm.gradle.publish")
    id("de.undercouch.download")
}

group = "space.kscience"
version = "0.2.0-dev-1"

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/dokka/")
    maven("https://dl.bintray.com/jetbrains/markdown/")
}

kotlin {
    explicitApiWarning()
    data class DownloadLinks(val gsl: String)

    val (nativeTarget, downloadLinks) = when (System.getProperty("os.name")) {
//        "Mac OS X" -> macosX64()

        "Linux" -> linuxX64() to DownloadLinks(
            gsl = "https://anaconda.org/conda-forge/gsl/2.6/download/linux-64/gsl-2.6-he838d99_2.tar.bz2"
        )

        else -> {
            logger.warn("Current OS cannot build any of kmath-gsl targets.")
            return@kotlin
        }
    }

    val thirdPartyDir =
        File("${System.getProperty("user.home")}/.konan/third-party/kmath-gsl-${project.property("version")}")

    val main by nativeTarget.compilations.getting {
        cinterops {
            val libgsl by creating
        }
    }

    val test by nativeTarget.compilations.getting

    val nativeMain by sourceSets.creating {
        val codegen by tasks.creating {
            matricesCodegen(kotlin.srcDirs.first().absolutePath + "/_Matrices.kt")
            vectorsCodegen(kotlin.srcDirs.first().absolutePath + "/_Vectors.kt")
        }

        kotlin.srcDirs(files().builtBy(codegen))

        dependencies {
            api("space.kscience:kmath-complex:0.2.0")
        }
    }

    val nativeTest by sourceSets.creating {
        dependsOn(nativeMain)
    }

    main.defaultSourceSet.dependsOn(nativeMain)
    test.defaultSourceSet.dependsOn(nativeTest)

    val downloadGsl by tasks.creating(Download::class) {
        val url = downloadLinks.gsl
        src(url)
        dest(File(thirdPartyDir, "libgsl.tar.bz2"))
        overwrite(false)
    }

    val extractGsl by tasks.creating(Exec::class) {
        dependsOn(downloadGsl)
        workingDir(thirdPartyDir)
        commandLine("tar", "-xf", downloadGsl.dest)
    }

    val writeDefFile by tasks.creating {
        val file = main.cinterops["libgsl"].defFile
        file.parentFile.mkdirs()
        file.createNewFile()

        file.writeText("""
                    package=org.gnu.gsl
                    headers=gsl/gsl_blas.h gsl/gsl_linalg.h gsl/gsl_permute_matrix.h gsl/gsl_matrix.h gsl/gsl_vector.h gsl/gsl_errno.h
                    staticLibraries=libgsl.a libgslcblas.a
                    compilerOpts=-I${thirdPartyDir}/include/
                    libraryPaths=${thirdPartyDir}/lib/

                """.trimIndent())

        dependsOn(extractGsl)
    }

    tasks[main.cinterops["libgsl"].interopProcessingTaskName].dependsOn(writeDefFile)

    nativeTarget.binaries {
        all {
            optimized = true
            debuggable = false
        }
    }
}

readme {
    description = "Linear Algebra classes implemented with GNU Scientific Library"
    maturity = Maturity.PROTOTYPE
    readmeTemplate = file("docs/templates/README-TEMPLATE.md")
    propertyByTemplate("artifact", file("docs/templates/ARTIFACT-TEMPLATE.md"))

    feature(
        id = "matrix-contexts",
        description = "Matrix Contexts over Double, Float, and Complex implemented with GSL",
        ref = "src/nativeMain/kotlin/GslMatrixContext.kt"
    )
}

ksciencePublish {
    spaceRepo = "https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven"
}
