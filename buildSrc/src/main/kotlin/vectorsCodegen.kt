package space.kscience.kmath.gsl.codegen

import org.intellij.lang.annotations.Language
import java.io.File

private fun Appendable.createVectorClass(
    cTypeName: String,
    kotlinTypeName: String,
) {
    fun fn(pattern: String) = fn(pattern, cTypeName)
    val className = "Gsl${kotlinTypeName}Vector"
    val structName = sn("gsl_vectorR", cTypeName)

    @Language("kotlin") val text =
        """internal class $className(
    override val rawNativeHandle: CPointer<$structName>, 
    scope: AutofreeScope, 
    owned: Boolean,
) : GslVector<$kotlinTypeName, $structName>(scope, owned) {
    override val size: Int get() = nativeHandle.pointed.size.toInt()
    override operator fun get(index: Int): $kotlinTypeName = ${fn("gsl_vectorRget")}(nativeHandle, index.toULong())
    override operator fun set(index: Int, value: $kotlinTypeName): Unit = ${fn("gsl_vectorRset")}(nativeHandle, index.toULong(), value)

    override fun copy(): $className {
        val new = checkNotNull(${fn("gsl_vectorRalloc")}(size.toULong()))
        ${fn("gsl_vectorRmemcpy")}(new, nativeHandle)
        return ${className}(new, scope, true)
    }

    override fun close(): Unit = ${fn("gsl_vectorRfree")}(nativeHandle)
}"""

    appendLine(text)
    appendLine()
}

/**
 * Generates vectors source code for kmath-gsl.
 */
fun vectorsCodegen(outputFile: File): Unit = outputFile.run {
    parentFile.mkdirs()

    writer().use { w ->
        w.appendLine("package space.kscience.kmath.gsl")
        w.appendLine()
        w.appendLine("import kotlinx.cinterop.*")
        w.appendLine("import org.gnu.gsl.*")
        w.appendLine()
        w.createVectorClass("double", "Double")
        w.createVectorClass("float", "Float")
    }
}
