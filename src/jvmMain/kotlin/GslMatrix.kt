package space.kscience.kmath.gsl

import jdk.incubator.foreign.MemoryAddress
import space.kscience.kmath.linear.Matrix
import space.kscience.kmath.misc.PerformancePitfall
import space.kscience.kmath.structures.asSequence

/**
 * Wraps gsl_matrix_* objects from GSL.
 */
internal abstract class GslMatrix<T : Any> internal constructor(rawNativeHandle: MemoryAddress, scope: DeferScope, owned: Boolean) :
    GslObject(rawNativeHandle, scope, owned), Matrix<T> {
    abstract operator fun set(i: Int, j: Int, value: T)
    abstract fun copy(): GslMatrix<T>

    @OptIn(PerformancePitfall::class)
    override fun toString(): String = if (rowNum <= 5 && colNum <= 5)
        "Matrix(rowsNum = $rowNum, colNum = $colNum)\n" +
                rows.joinToString(prefix = "(", postfix = ")", separator = "\n ") { buffer ->
                    buffer.asSequence().joinToString(separator = "\t") { it.toString() }
                }
    else
        "Matrix(rowsNum = $rowNum, colNum = $colNum)"
}
