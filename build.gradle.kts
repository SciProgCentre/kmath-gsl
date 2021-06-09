@file:Suppress("UNUSED_VARIABLE")

import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.dokka.gradle.DokkaTask
import ru.mipt.npm.gradle.Maturity
import space.kscience.kmath.gsl.codegen.matricesCodegen
import space.kscience.kmath.gsl.codegen.vectorsCodegen

plugins {
    `maven-publish`
    kotlin("multiplatform")
    id("ru.mipt.npm.gradle.project")
    id("ru.mipt.npm.gradle.common")
    id("de.undercouch.download")
}

group = "space.kscience"
version = "0.3.0-dev-1"

repositories.mavenCentral()

kotlin {
    explicitApiWarning()
    data class DownloadLinks(val gsl: String)

    val (nativeTarget, downloadLinks) = when (System.getProperty("os.name")) {
        "Linux" -> linuxX64() to DownloadLinks(
            gsl = "https://anaconda.org/conda-forge/gsl/2.6/download/linux-64/gsl-2.6-he838d99_2.tar.bz2",
        )

        else -> {
            logger.warn("Current OS cannot build any of kmath-gsl targets.")
            return@kotlin
        }
    }

    val thirdPartyDir =
        File("${System.getProperty("user.home")}/.konan/third-party/kmath-gsl-${project.property("version")}")


    val main by nativeTarget.compilations.getting
    val test by nativeTarget.compilations.getting

    val libgsl by main.cinterops.creating

    val nativeMain by sourceSets.creating {
        val codegen by tasks.creating {
            matricesCodegen(kotlin.srcDirs.first().absolutePath + "/_Matrices.kt")
            vectorsCodegen(kotlin.srcDirs.first().absolutePath + "/_Vectors.kt")
        }

        kotlin.srcDirs(files().builtBy(codegen))

        dependencies {
            api("space.kscience:kmath-complex:0.3.0-dev-12")
        }
    }

    val nativeTest by sourceSets.creating {
        dependsOn(nativeMain)
    }

    main.defaultSourceSet.dependsOn(nativeMain)
    test.defaultSourceSet.dependsOn(nativeTest)

    val downloadGsl by tasks.creating(Download::class) {
        src(downloadLinks.gsl)
        dest(File(thirdPartyDir, "libgsl.tar.bz2"))
        overwrite(false)
    }

    val extractGsl by tasks.creating(Exec::class) {
        dependsOn(downloadGsl)
        workingDir(thirdPartyDir)
        commandLine("tar", "-xf", downloadGsl.dest)
    }

    val writeDefFile by tasks.creating {
        val file = libgsl.defFile
        file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()

        file.writeText("""
                    package=org.gnu.gsl
                    headers=gsl/gsl_blas.h gsl/gsl_linalg.h gsl/gsl_permute_matrix.h gsl/gsl_matrix.h gsl/gsl_vector.h gsl/gsl_errno.h
                    linkerOpts = -L/usr/lib64 -L/usr/lib/x86_64-linux-gnu -lblas
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
        description =
        "LinearSpace implementations for Double, Float, and Complex matrices and vectors implemented with GSL",
        ref = "src/nativeMain/kotlin/GslLinearSpace.kt"
    )
}

ksciencePublish {
    github("kmath-gsl")
    space()
}

apiValidation {
    nonPublicMarkers.add("space.kscience.kmath.misc.UnstableKMathAPI")
}

afterEvaluate {
    tasks.withType<DokkaTask> {
        dokkaSourceSets.all {
            val readmeFile = File(projectDir, "./README.md")
            if (readmeFile.exists())
                includes.setFrom(includes + readmeFile.absolutePath)

            externalDocumentationLink("https://mipt-npm.github.io/kmath/kmath-core/kmath-core/")
            externalDocumentationLink("https://mipt-npm.github.io/kmath/kmath-memory/kmath-memory/")
            externalDocumentationLink("https://mipt-npm.github.io/kmath/kmath-complex/kmath-complex/")
        }
    }
}
