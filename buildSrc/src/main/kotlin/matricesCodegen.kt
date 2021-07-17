package space.kscience.kmath.gsl.codegen

import org.intellij.lang.annotations.Language
import java.io.File

private fun Appendable.createMatrixClass(
    cTypeName: String,
    kotlinTypeName: String,
) {
    fun fn(pattern: String) = fn(pattern, cTypeName)
    val className = "Gsl${kotlinTypeName}Matrix"
    val structName = sn("gsl_matrixR", cTypeName)

    @Language("kotlin") val text = """internal class $className(
    override val rawNativeHandle: CPointer<$structName>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<$kotlinTypeName, $structName>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<$kotlinTypeName>>
        get() = List(rowNum) { r ->
            Gsl${kotlinTypeName}Vector(
                ${fn("gsl_matrixRrow")}(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<$kotlinTypeName>>
        get() = List(rowNum) { c ->
            Gsl${kotlinTypeName}Vector(
                ${fn("gsl_matrixRcolumn")}(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): $kotlinTypeName = 
        ${fn("gsl_matrixRget")}(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: ${kotlinTypeName}): Unit =
        ${fn("gsl_matrixRset")}(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): $className {
        val new = checkNotNull(${fn("gsl_matrixRalloc")}(rowNum.toULong(), colNum.toULong()))
        ${fn("gsl_matrixRmemcpy")}(new, nativeHandle)
        return $className(new, scope, true)
    }

    override fun close(): Unit = ${fn("gsl_matrixRfree")}(nativeHandle)
}"""
    appendLine(text)
    appendLine()
}

/**
 * Generates matrices source code for kmath-gsl.
 */
fun matricesCodegen(outputFile: File): Unit = outputFile.run {
    parentFile.mkdirs()
    writer().use {
        it.appendLine("package space.kscience.kmath.gsl")
        it.appendLine()
        it.appendLine("import kotlinx.cinterop.*")
        it.appendLine("import org.gnu.gsl.*")
        it.appendLine("import space.kscience.kmath.misc.PerformancePitfall")
        it.appendLine("import space.kscience.kmath.structures.*")
        it.appendLine()
        it.createMatrixClass("double", "Double")
        it.createMatrixClass("float", "Float")
    }
}
