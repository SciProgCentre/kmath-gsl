package space.kscience.kmath.gsl

import jdk.incubator.foreign.MemoryAddress
import org.gnu.gsl.GSL_1.*
import org.gnu.gsl._gsl_vector_view
import org.gnu.gsl.gsl_matrix
import space.kscience.kmath.misc.PerformancePitfall
import space.kscience.kmath.structures.Buffer

internal class GslDoubleMatrix(
    rawNativeHandle: MemoryAddress,
    scope: DeferScope,
    owned: Boolean,
) : GslMatrix<Double>(rawNativeHandle, scope, owned) {
    override val rowNum: Int
        get() = gsl_matrix.`size1$get`(gsl_matrix.ofAddress(rawNativeHandle, scope)).toInt()

    override val colNum: Int
        get() = gsl_matrix.`size2$get`(gsl_matrix.ofAddress(rawNativeHandle, scope)).toInt()

    @PerformancePitfall
    override val rows: List<Buffer<Double>>
        get() = List(rowNum) { r ->
            GslDoubleVector(
                _gsl_vector_view.`vector$slice`(gsl_matrix_row(scope, nativeHandle, r.toLong())).address(),
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<Double>>
        get() = List(rowNum) { c ->
            GslDoubleVector(
                _gsl_vector_view.`vector$slice`(gsl_matrix_column(scope, nativeHandle, c.toLong())).address(),
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): Double =
        gsl_matrix_get(nativeHandle, i.toLong(), j.toLong())

    override operator fun set(i: Int, j: Int, value: Double): Unit =
        gsl_matrix_set(nativeHandle, i.toLong(), j.toLong(), value)

    override fun copy(): GslDoubleMatrix {
        val new = checkNotNull(gsl_matrix_alloc(rowNum.toLong(), colNum.toLong()))
        gsl_matrix_memcpy(new, nativeHandle)
        return GslDoubleMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_free(nativeHandle)
}
