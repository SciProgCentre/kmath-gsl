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

internal class GslShortMatrix(
    override val rawNativeHandle: CPointer<gsl_matrix_short>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<Short, gsl_matrix_short>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<Short>>
        get() = List(rowNum) { r ->
            GslShortVector(
                gsl_matrix_short_row(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<Short>>
        get() = List(rowNum) { c ->
            GslShortVector(
                gsl_matrix_short_column(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): Short = 
        gsl_matrix_short_get(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: Short): Unit =
        gsl_matrix_short_set(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): GslShortMatrix {
        val new = checkNotNull(gsl_matrix_short_alloc(rowNum.toULong(), colNum.toULong()))
        gsl_matrix_short_memcpy(new, nativeHandle)
        return GslShortMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_short_free(nativeHandle)
}

internal class GslUShortMatrix(
    override val rawNativeHandle: CPointer<gsl_matrix_ushort>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<UShort, gsl_matrix_ushort>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<UShort>>
        get() = List(rowNum) { r ->
            GslUShortVector(
                gsl_matrix_ushort_row(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<UShort>>
        get() = List(rowNum) { c ->
            GslUShortVector(
                gsl_matrix_ushort_column(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): UShort = 
        gsl_matrix_ushort_get(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: UShort): Unit =
        gsl_matrix_ushort_set(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): GslUShortMatrix {
        val new = checkNotNull(gsl_matrix_ushort_alloc(rowNum.toULong(), colNum.toULong()))
        gsl_matrix_ushort_memcpy(new, nativeHandle)
        return GslUShortMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_ushort_free(nativeHandle)
}

internal class GslLongMatrix(
    override val rawNativeHandle: CPointer<gsl_matrix_long>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<Long, gsl_matrix_long>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<Long>>
        get() = List(rowNum) { r ->
            GslLongVector(
                gsl_matrix_long_row(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<Long>>
        get() = List(rowNum) { c ->
            GslLongVector(
                gsl_matrix_long_column(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): Long = 
        gsl_matrix_long_get(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: Long): Unit =
        gsl_matrix_long_set(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): GslLongMatrix {
        val new = checkNotNull(gsl_matrix_long_alloc(rowNum.toULong(), colNum.toULong()))
        gsl_matrix_long_memcpy(new, nativeHandle)
        return GslLongMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_long_free(nativeHandle)
}

internal class GslULongMatrix(
    override val rawNativeHandle: CPointer<gsl_matrix_ulong>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<ULong, gsl_matrix_ulong>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<ULong>>
        get() = List(rowNum) { r ->
            GslULongVector(
                gsl_matrix_ulong_row(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<ULong>>
        get() = List(rowNum) { c ->
            GslULongVector(
                gsl_matrix_ulong_column(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): ULong = 
        gsl_matrix_ulong_get(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: ULong): Unit =
        gsl_matrix_ulong_set(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): GslULongMatrix {
        val new = checkNotNull(gsl_matrix_ulong_alloc(rowNum.toULong(), colNum.toULong()))
        gsl_matrix_ulong_memcpy(new, nativeHandle)
        return GslULongMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_ulong_free(nativeHandle)
}

internal class GslIntMatrix(
    override val rawNativeHandle: CPointer<gsl_matrix_int>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<Int, gsl_matrix_int>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<Int>>
        get() = List(rowNum) { r ->
            GslIntVector(
                gsl_matrix_int_row(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<Int>>
        get() = List(rowNum) { c ->
            GslIntVector(
                gsl_matrix_int_column(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): Int = 
        gsl_matrix_int_get(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: Int): Unit =
        gsl_matrix_int_set(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): GslIntMatrix {
        val new = checkNotNull(gsl_matrix_int_alloc(rowNum.toULong(), colNum.toULong()))
        gsl_matrix_int_memcpy(new, nativeHandle)
        return GslIntMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_int_free(nativeHandle)
}

internal class GslUIntMatrix(
    override val rawNativeHandle: CPointer<gsl_matrix_uint>,
    scope: AutofreeScope,
    owned: Boolean,
) : GslMatrix<UInt, gsl_matrix_uint>(scope, owned) {
    override val rowNum: Int
        get() = nativeHandle.pointed.size1.toInt()

    override val colNum: Int
        get() = nativeHandle.pointed.size2.toInt()

    @PerformancePitfall
    override val rows: List<Buffer<UInt>>
        get() = List(rowNum) { r ->
            GslUIntVector(
                gsl_matrix_uint_row(nativeHandle, r.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    @PerformancePitfall
    override val columns: List<Buffer<UInt>>
        get() = List(rowNum) { c ->
            GslUIntVector(
                gsl_matrix_uint_column(nativeHandle, c.toULong()).placeTo(scope).pointed.vector.ptr,
                scope,
                false,
            )
        }

    override operator fun get(i: Int, j: Int): UInt = 
        gsl_matrix_uint_get(nativeHandle, i.toULong(), j.toULong())

    override operator fun set(i: Int, j: Int, value: UInt): Unit =
        gsl_matrix_uint_set(nativeHandle, i.toULong(), j.toULong(), value)

    override fun copy(): GslUIntMatrix {
        val new = checkNotNull(gsl_matrix_uint_alloc(rowNum.toULong(), colNum.toULong()))
        gsl_matrix_uint_memcpy(new, nativeHandle)
        return GslUIntMatrix(new, scope, true)
    }

    override fun close(): Unit = gsl_matrix_uint_free(nativeHandle)
}

