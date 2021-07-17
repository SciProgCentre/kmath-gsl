import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.dokka.Platform
import org.jetbrains.dokka.gradle.*
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinNativeCompile
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.HostManager
import space.kscience.kmath.gsl.codegen.matricesCodegen
import space.kscience.kmath.gsl.codegen.vectorsCodegen
import java.net.URL
import space.kscience.gradle.*

plugins {
    `maven-publish`
    alias(libs.plugins.download)
    alias(kscienceLibs.plugins.gradle.project)
    alias(kscienceLibs.plugins.kotlin.multiplatform)
}

group = "space.kscience"
version = "0.3.0-dev-5"

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

kotlin {
    explicitApi()

    data class DownloadLinks(val gsl: String?)

    val nativeTargets = setOf(
        linuxX64(),
        mingwX64(),
        macosX64(),
    )

    val downloadLinks = when (HostManager.hostOs()) {
        "linux" -> DownloadLinks(
            gsl = "https://anaconda.org/conda-forge/gsl/2.7/download/linux-64/gsl-2.7-he838d99_0.tar.bz2",
        )

        "windows" -> DownloadLinks(gsl = null)

        "osx" -> DownloadLinks(
            gsl = "https://anaconda.org/conda-forge/gsl/2.7/download/osx-64/gsl-2.7-h93259b0_0.tar.bz2"
        )

        else -> {
            logger.warn("Current OS cannot build any of kmath-gsl targets.")
            return@kotlin
        }
    }

    val thirdPartyDir =
        File("${System.getProperty("user.home")}/.konan/third-party/kmath-gsl-${project.property("version")}")

    val downloadGsl by tasks.creating(Download::class) {
        if (downloadLinks.gsl == null) {
            enabled = false
            return@creating
        }

        src(downloadLinks.gsl)
        dest(thirdPartyDir.resolve("libgsl.tar.bz2"))
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
        val file = projectDir.resolve("src/nativeInterop/cinterop/libgsl.def")
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
                    
                    linkerOpts.osx=-L/opt/local/lib -L/usr/local/lib -lblas
                    staticLibraries.osx=libgsl.a libgslcblas.a
                    compilerOpts.osx=-I${thirdPartyDir}/include/
                    libraryPaths.osx=${thirdPartyDir}/lib/

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

    sourceSets {
        all {
            with(languageSettings) {
                progressiveMode = true
                optIn("kotlin.time.ExperimentalTime")
            }
        }

        commonMain {
            dependencies {
                api("space.kscience:kmath-complex:0.3.0")
            }
        }

        val nativeMain by creating {
            val codegen by tasks.creating {
                matricesCodegen(kotlin.srcDirs.first().resolve("_Matrices.kt"))
                vectorsCodegen(kotlin.srcDirs.first().resolve("_Vectors.kt"))
            }

            kotlin.srcDirs(files().builtBy(codegen))
            dependsOn(commonMain.get())
        }

        val nativeTest by creating {
            dependsOn(nativeMain)
            dependsOn(commonTest.get())
        }

        configure(nativeTargets) {
            val main by compilations.getting
            val test by compilations.getting

            configure(main.kotlinSourceSets) {
                dependsOn(nativeMain)
            }

            configure(test.kotlinSourceSets) {
                dependsOn(nativeTest)
            }

            val libgsl by main.cinterops.creating
            tasks[libgsl.interopProcessingTaskName].dependsOn(writeDefFile)
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }
}

tasks {
    withType<CInteropProcess> {
        onlyIf {
            konanTarget == HostManager.host
        }
    }

    withType<AbstractKotlinNativeCompile<*, *, *>> {
        onlyIf {
            compilation.konanTarget == HostManager.host
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
    pom("https://github.com/SciProgCentre/${rootProject.name}") {
        useApache2Licence()
        useSPCTeam()
    }

    github(rootProject.name, "SciProgCentre", addToRelease = false)

    space(
        if (isInDevelopment) {
            "https://maven.pkg.jetbrains.space/spc/p/sci/dev"
        } else {
            "https://maven.pkg.jetbrains.space/spc/p/sci/maven"
        },
    )
}

apiValidation.nonPublicMarkers.add("space.kscience.kmath.misc.UnstableKMathAPI")

tasks.withType<DokkaTask> {
    dokkaSourceSets.register("nativeMain") {
        displayName.set("Native")
        platform.set(Platform.native)
        sourceRoots.from(kotlin.sourceSets.getByName("nativeMain").kotlin.srcDirs)
        val readmeFile = projectDir.resolve("README.md")
        if (readmeFile.exists()) includes.from(readmeFile)
        val kotlinDirPath = "src/$name/kotlin"
        val kotlinDir = file(kotlinDirPath)

        if (kotlinDir.exists()) sourceLink {
            localDirectory.set(kotlinDir)

            remoteUrl.set(
                URL("https://github.com/SciProgCentre/${rootProject.name}/tree/master/$kotlinDirPath")
            )
        }

        externalDocumentationLink(
            "https://sciprogcentre.github.io/kmath/kmath-core/",
            "https://sciprogcentre.github.io/kmath/package-list",
        )

        externalDocumentationLink(
            "https://sciprogcentre.github.io/kmath/kmath-memory/",
            "https://sciprogcentre.github.io/kmath/package-list",
        )

        externalDocumentationLink(
            "https://sciprogcentre.github.io/kmath/kmath-complex/",
            "https://sciprogcentre.github.io/kmath/package-list",
        )
    }
}
