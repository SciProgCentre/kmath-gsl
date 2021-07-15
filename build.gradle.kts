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
    id("de.undercouch.download")
}

group = "space.kscience"
version = "0.3.0-dev-2"

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

kotlin {
    explicitApi()

    sourceSets.all {
        with(languageSettings) {
            progressiveMode = true
            useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }
    }

    sourceSets.commonMain {
        dependencies {
            api("space.kscience:kmath-complex:0.3.0-dev-12")
        }
    }

    data class DownloadLinks(val gsl: String?)

    val osName = System.getProperty("os.name")
    val isWindows = osName.startsWith("Windows")

    val (nativeTarget, downloadLinks) = when {
        osName == "Linux" -> linuxX64() to DownloadLinks(
            gsl = "https://anaconda.org/conda-forge/gsl/2.7/download/linux-64/gsl-2.7-he838d99_0.tar.bz2",
        )

        isWindows -> mingwX64() to DownloadLinks(
            gsl = null,
        )

        else -> {
            logger.warn("Current OS cannot build any of kmath-gsl targets.")
            return@kotlin
        }
    }

    val thirdPartyDir =
        File("${System.getProperty("user.home")}/.konan/third-party/kmath-gsl-${project.property("version")}")

    val main by nativeTarget.compilations.getting

    val test by nativeTarget.compilations.getting {
        defaultSourceSet.dependsOn(main.defaultSourceSet)
    }

    val libgsl by main.cinterops.creating

    val nativeMain by sourceSets.creating {
        val codegen by tasks.creating {
            matricesCodegen(kotlin.srcDirs.first().absolutePath + "/_Matrices.kt")
            vectorsCodegen(kotlin.srcDirs.first().absolutePath + "/_Vectors.kt")
        }

        kotlin.srcDirs(files().builtBy(codegen))
        dependsOn(sourceSets.commonMain.get())
    }

    val nativeTest by sourceSets.creating {
        dependsOn(nativeMain)
        dependsOn(sourceSets.commonTest.get())
    }

    main.defaultSourceSet.dependsOn(nativeMain)
    test.defaultSourceSet.dependsOn(nativeTest)

    val downloadGsl by tasks.creating(Download::class) {
        if (downloadLinks.gsl == null) {
            enabled = false
            return@creating
        }

        src(downloadLinks.gsl)
        dest(File(thirdPartyDir, "libgsl.tar.bz2"))
        overwrite(false)
    }

    val extractGsl by tasks.creating(Exec::class) {
        if (downloadLinks.gsl == null) {
            enabled = false
            return@creating
        }

        dependsOn(downloadGsl)
        workingDir(thirdPartyDir)
        commandLine("tar", "-xf", downloadGsl.dest)
    }

    val writeDefFile by tasks.creating {
        val file = libgsl.defFile
        file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()

        file.writeText(
            """
                    package=org.gnu.gsl
                    headers=gsl/gsl_blas.h gsl/gsl_linalg.h gsl/gsl_permute_matrix.h gsl/gsl_matrix.h gsl/gsl_vector.h gsl/gsl_errno.h

                    linkerOpts.linux=-L/usr/lib64 -L/usr/lib/x86_64-linux-gnu -lblas
                    staticLibraries.linux=libgsl.a libgslcblas.a
                    compilerOpts.linux=-I${thirdPartyDir}/include/
                    libraryPaths.linux=${thirdPartyDir}/lib/

                    linkerOpts.mingw=-LC:/msys64/mingw64/lib/ -LC:/msys64/mingw64/bin/
                    staticLibraries.mingw=libgsl.a libgslcblas.a
                    compilerOpts.mingw=-IC:/msys64/mingw64/include/
                    libraryPaths.mingw=C:/msys64/mingw64/lib/
                    ---
                    inline int gsl_matrix_float_scale2(gsl_matrix_float *a, const float x) {
                        return gsl_matrix_float_scale(a, x);
                    }

                """.trimIndent()
        )

        dependsOn(extractGsl)
    }

    tasks[main.cinterops["libgsl"].interopProcessingTaskName].dependsOn(writeDefFile)

    nativeTarget.binaries {
        all {
            optimized = false
            debuggable = true
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
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
        ref = "src/nativeMain/kotlin/GslLinearSpace.kt",
    ) { "LinearSpace implementations for Double, Float, and Complex matrices and vectors implemented with GSL" }
}

ksciencePublish {
    vcs("https://github.com/mipt-npm/kmath-gsl")
    space(publish = true)
}

apiValidation.nonPublicMarkers.add("space.kscience.kmath.misc.UnstableKMathAPI")

afterEvaluate {
    tasks.withType<DokkaTask> {
        dokkaSourceSets.all {
            val readmeFile = File(projectDir, "./README.md")
            if (readmeFile.exists()) includes.from(readmeFile)
            externalDocumentationLink("https://mipt-npm.github.io/kmath/kmath-core/kmath-core/")
            externalDocumentationLink("https://mipt-npm.github.io/kmath/kmath-memory/kmath-memory/")
            externalDocumentationLink("https://mipt-npm.github.io/kmath/kmath-complex/kmath-complex/")
        }
    }
}
