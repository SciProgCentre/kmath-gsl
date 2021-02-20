@file:Suppress("UNUSED_VARIABLE")

import de.undercouch.gradle.tasks.download.Download
import kscience.kmath.gsl.codegen.matricesCodegen
import kscience.kmath.gsl.codegen.vectorsCodegen
import ru.mipt.npm.gradle.Maturity

plugins {
    id("ru.mipt.npm.mpp")
    id("ru.mipt.npm.project")
    id("ru.mipt.npm.publish")
    id("de.undercouch.download")
}

group = "kscience.kmath"
version = "0.2.0-dev-7"

repositories {
    maven("https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven")
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
            val libgsl by creating {
                compilerOpts += "-I${thirdPartyDir}/include/"
            }
        }
    }

    val test by nativeTarget.compilations.getting

    val nativeMain by sourceSets.creating {
        val codegen by tasks.creating {
            matricesCodegen(kotlin.srcDirs.first().absolutePath + "/kscience/kmath/gsl/_Matrices.kt")
            vectorsCodegen(kotlin.srcDirs.first().absolutePath + "/kscience/kmath/gsl/_Vectors.kt")
        }

        kotlin.srcDirs(files().builtBy(codegen))

        dependencies {
            api("kscience.kmath:kmath-complex:0.2.0-dev-7")
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

    val extractGsl by tasks.creating(Copy::class) {
        dependsOn(downloadGsl)
        from(tarTree(downloadGsl.dest))
        into(thirdPartyDir)
    }

    tasks[nativeTarget.main.cinterops["libgsl"].interopProcessingTaskName].dependsOn(extractGsl)

    nativeTarget.binaries {
        all {
            linkerOpts(
                "-L${File(thirdPartyDir, "lib").absolutePath}",
                "-lgsl"
            )

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
        description = "Matrix Contexts over Double, Float and Complex implemented with GSL",
        ref = "src/nativeMain/kotlin/kscience/kmath/gsl/GslMatrixContext.kt"
    )
}

ksciencePublish {
    spaceRepo = "https://maven.pkg.jetbrains.space/mipt-npm/p/sci/maven"
}
