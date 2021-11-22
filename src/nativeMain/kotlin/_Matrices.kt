/*
 * Copyright 2021 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl

import kotlinx.cinterop.*
import org.gnu.gsl.*
import space.kscience.kmath.misc.PerformancePitfall
import space.kscience.kmath.structures.*

internal class GslDoubleMatrix(
    override val rawNativeHandle: CPointer<gsl_matrix>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<Double, gsl_matrix>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<Double>>
        get() = List(rowNum) { r ->
            GslDoubleVector(
                gsl_matrix_row(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<Double>>
        get() = List(rowNum) { c ->
            GslDoubleVector(
                gsl_matrix_column(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): Double = 
        gsl_matrix_get(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: Double): Unit =
        gsl_matrix_set(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): GslDoubleMatrix {
        val new = checkNotNull(gsl_matrix_alloc(rowNum.toULong(), colNum.toULong()))
        gsl_matrix_memcpy(new, nativeHandle)
        return GslDoubleMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_free(nativeHandle)
}

internal class GslFloatMatrix(
    override val rawNativeHandle: CPointer<gsl_matrix_float>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<Float, gsl_matrix_float>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<Float>>
        get() = List(rowNum) { r ->
            GslFloatVector(
                gsl_matrix_float_row(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<Float>>
        get() = List(rowNum) { c ->
            GslFloatVector(
                gsl_matrix_float_column(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): Float = 
        gsl_matrix_float_get(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: Float): Unit =
        gsl_matrix_float_set(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): GslFloatMatrix {
        val new = checkNotNull(gsl_matrix_float_alloc(rowNum.toULong(), colNum.toULong()))
        gsl_matrix_float_memcpy(new, nativeHandle)
        return GslFloatMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_float_free(nativeHandle)
}

