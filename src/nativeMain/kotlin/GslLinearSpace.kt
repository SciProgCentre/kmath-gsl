/*
 * Copyright 2021 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl

import kotlinx.cinterop.*
import org.gnu.gsl.*
import space.kscience.kmath.complex.Complex
import space.kscience.kmath.complex.ComplexField
import space.kscience.kmath.complex.toComplex
import space.kscience.kmath.linear.*
import space.kscience.kmath.misc.PerformancePitfall
import space.kscience.kmath.misc.UnstableKMathAPI
import space.kscience.kmath.nd.StructureFeature
import space.kscience.kmath.operations.*
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.reflect.cast

internal inline fun <T : Any, A : Ring<T>, H : CStructVar> GslMatrix<T, H>.fill(
    a: A,
    initializer: A.(Int, Int) -> T,
): GslMatrix<T, H> =
    apply {
        for (col in 0 until colNum) {
            for (row in 0 until rowNum) this[row, col] = a.initializer(row, col)
        }
    }

internal inline fun <T : Any, A : Ring<T>, H : CStructVar> GslVector<T, H>.fill(
    a: A,
    initializer: A.(Int) -> T,
): GslVector<T, H> =
    apply {
        for (index in 0 until size) this[index] = a.initializer(index)
    }

/**
 * Represents matrix context implementing where all the operations are delegated to GSL.
 */
public abstract class GslLinearSpace<T : Any, out A : Ring<T>, H1 : CStructVar, H2 : CStructVar> internal constructor(
    internal val scope: AutofreeScope,
) : AutofreeScope(), LinearSpace<T, A> {
    init {
        ensureHasGslErrorHandler()
    }

    /**
     * Converts this matrix to GSL one.
     */
    @Suppress("UNCHECKED_CAST")
    public fun Matrix<T>.toGsl(): GslMatrix<T, H1> = if (this is GslMatrix<*, *>)
        this as GslMatrix<T, H1>
    else
        buildMatrix(rowNum, colNum) { i, j -> this@toGsl[i, j] }

    /**
     * Converts this point to GSL one.
     */
    @Suppress("UNCHECKED_CAST")
    public fun Point<T>.toGsl(): GslVector<T, H2> =
        (if (this is GslVector<*, *>) this as GslVector<T, H2> else produceDirtyVector(size).fill(elementAlgebra) { this@toGsl[it] }).copy()

    internal abstract fun produceDirtyMatrix(rows: Int, columns: Int): GslMatrix<T, H1>
    internal abstract fun produceDirtyVector(size: Int): GslVector<T, H2>

    override fun buildMatrix(rows: Int, columns: Int, initializer: A.(i: Int, j: Int) -> T): GslMatrix<T, H1> =
        produceDirtyMatrix(rows, columns).fill(elementAlgebra, initializer)

    override fun buildVector(size: Int, initializer: A.(Int) -> T): GslVector<T, H2> =
        produceDirtyVector(size).fill(elementAlgebra, initializer)

    override fun alloc(size: Long, align: Int): NativePointed = scope.alloc(size, align)
    override fun alloc(size: Int, align: Int): NativePointed = scope.alloc(size, align)
}

/**
 * Represents [Double] matrix context implementing where all the operations are delegated to GSL.
 */
@Suppress("OVERRIDE_BY_INLINE")
public class GslDoubleLinearSpace(scope: AutofreeScope) :
    GslLinearSpace<Double, DoubleField, gsl_matrix, gsl_vector>(scope) {
    override inline val elementAlgebra: DoubleField
        get() = DoubleField

    override fun produceDirtyMatrix(rows: Int, columns: Int): GslMatrix<Double, gsl_matrix> = GslDoubleMatrix(
        rawNativeHandle = checkNotNull(gsl_matrix_alloc(rows.toULong(), columns.toULong())),
        scope = scope,
        owned = true,
    )

    override fun produceDirtyVector(size: Int): GslVector<Double, gsl_vector> =
        GslDoubleVector(rawNativeHandle = checkNotNull(gsl_vector_alloc(size.toULong())), scope = scope, owned = true)

    override fun Matrix<Double>.dot(other: Matrix<Double>): GslMatrix<Double, gsl_matrix> {
        val x = toGsl().nativeHandle
        val a = other.toGsl().nativeHandle
        val result = checkNotNull(gsl_matrix_calloc(a.pointed.size1, a.pointed.size2))
        gsl_blas_dgemm(CblasNoTrans, CblasNoTrans, 1.0, x, a, 1.0, result)
        return GslDoubleMatrix(rawNativeHandle = result, scope = scope, owned = true)
    }

    override fun Matrix<Double>.dot(vector: Point<Double>): GslVector<Double, gsl_vector> {
        val x = toGsl().nativeHandle
        val a = vector.toGsl().nativeHandle
        val result = checkNotNull(gsl_vector_calloc(a.pointed.size))
        gsl_blas_dgemv(CblasNoTrans, 1.0, x, a, 1.0, result)
        return GslDoubleVector(rawNativeHandle = result, scope = scope, owned = true)
    }

    override fun Matrix<Double>.times(value: Double): GslMatrix<Double, gsl_matrix> {
        val g1 = toGsl().copy()
        gsl_matrix_scale(g1.nativeHandle, value)
        return g1
    }

    override fun Matrix<Double>.plus(other: Matrix<Double>): GslMatrix<Double, gsl_matrix> {
        val g1 = toGsl().copy()
        gsl_matrix_add(g1.nativeHandle, other.toGsl().nativeHandle)
        return g1
    }

    override fun Matrix<Double>.minus(other: Matrix<Double>): Matrix<Double> {
        val g1 = toGsl().copy()
        gsl_matrix_sub(g1.nativeHandle, other.toGsl().nativeHandle)
        return g1
    }

    @OptIn(PerformancePitfall::class)
    @Suppress("IMPLICIT_CAST_TO_ANY")
    @UnstableKMathAPI
    override fun <F : StructureFeature> getFeature(structure: Matrix<Double>, type: KClass<out F>): F? =
        when (type) {
            LupDecompositionFeature::class, DeterminantFeature::class -> object : LupDecompositionFeature<Double>,
                DeterminantFeature<Double>, InverseMatrixFeature<Double> {
                private val lups by lazy {
                    val lu = structure.toGsl().copy()
                    val n = structure.rowNum

                    val perm = GslPermutation(
                        rawNativeHandle = checkNotNull(gsl_permutation_alloc(n.toULong())),
                        scope = scope,
                        owned = true,
                    )

                    val signum = memScoped {
                        val i = alloc<IntVar>()
                        gsl_linalg_LU_decomp(lu.nativeHandle, perm.nativeHandle, i.ptr)
                        i.value
                    }

                    Triple(lu, perm, signum)
                }

                override val p by lazy {
                    val n = structure.rowNum
                    val one = buildMatrix(n, n) { i, j -> if (i == j) 1.0 else 0.0 }
                    val perm = buildMatrix(n, n) { _, _ -> 0.0 }

                    for (j in 0 until lups.second.size)
                        gsl_matrix_set_col(perm.nativeHandle,
                            j.toULong(),
                            one.columns[lups.second[j]].toGsl().nativeHandle)

                    perm
                }

                override val l by lazy {
                    VirtualMatrix(lups.first.shape[0], lups.first.shape[1]) { i, j ->
                        when {
                            j < i -> lups.first[i, j]
                            j == i -> 1.0
                            else -> 0.0
                        }
                    } + LFeature
                }

                override val u by lazy {
                    VirtualMatrix(
                        lups.first.shape[0],
                        lups.first.shape[1],
                    ) { i, j -> if (j >= i) lups.first[i, j] else 0.0 } + UFeature
                }

                override val determinant by lazy { gsl_linalg_LU_det(lups.first.nativeHandle, lups.third) }

                override val inverse by lazy {
                    val inv = lups.first.copy()
                    gsl_linalg_LU_invx(inv.nativeHandle, lups.second.nativeHandle)
                    inv
                }
            }

            CholeskyDecompositionFeature::class -> object : CholeskyDecompositionFeature<Double> {
                override val l: Matrix<Double> by lazy {
                    val chol = structure.toGsl().copy()
                    gsl_linalg_cholesky_decomp(chol.nativeHandle)
                    chol
                }
            }

            QRDecompositionFeature::class -> object : QRDecompositionFeature<Double> {
                private val qr by lazy {
                    val a = structure.toGsl().copy()
                    val q = buildMatrix(structure.rowNum, structure.rowNum) { _, _ -> 0.0 }
                    val r = buildMatrix(structure.rowNum, structure.colNum) { _, _ -> 0.0 }

                    if (structure.rowNum < structure.colNum) {
                        val tau = buildVector(min(structure.rowNum, structure.colNum)) { 0.0 }
                        gsl_linalg_QR_decomp(a.nativeHandle, tau.nativeHandle)
                        gsl_linalg_QR_unpack(a.nativeHandle, tau.nativeHandle, q.nativeHandle, r.nativeHandle)
                    } else {
                        val t = buildMatrix(structure.colNum, structure.colNum) { _, _ -> 0.0 }
                        gsl_linalg_QR_decomp_r(a.nativeHandle, t.nativeHandle)
                        gsl_linalg_QR_unpack_r(a.nativeHandle, t.nativeHandle, q.nativeHandle, r.nativeHandle)
                    }

                    q to r
                }

                override val q: Matrix<Double> get() = qr.first
                override val r: Matrix<Double> get() = qr.second
            }

            else -> super.getFeature(structure, type)
        }?.let(type::cast)
}

/**
 * Invokes [block] inside newly created [GslDoubleLinearSpace] which is disposed when the block is invoked.
 */
public fun <R> GslDoubleLinearSpace(block: GslDoubleLinearSpace.() -> R): R =
    memScoped { GslDoubleLinearSpace(this).block() }

/**
 * Represents [Float] matrix context implementing where all the operations are delegated to GSL.
 */
public class GslFloatLinearSpace(scope: AutofreeScope) :
    GslLinearSpace<Float, FloatField, gsl_matrix_float, gsl_vector_float>(scope) {
    override val elementAlgebra: FloatField
        get() = FloatField

    override fun produceDirtyMatrix(rows: Int, columns: Int): GslMatrix<Float, gsl_matrix_float> =
        GslFloatMatrix(
            rawNativeHandle = checkNotNull(gsl_matrix_float_alloc(rows.toULong(), columns.toULong())),
            scope = scope,
            owned = true,
        )

    override fun produceDirtyVector(size: Int): GslVector<Float, gsl_vector_float> = GslFloatVector(
        rawNativeHandle = checkNotNull(value = gsl_vector_float_alloc(size.toULong())),
        scope = scope,
        owned = true,
    )

    override fun Matrix<Float>.dot(other: Matrix<Float>): GslMatrix<Float, gsl_matrix_float> {
        val x = toGsl().nativeHandle
        val a = other.toGsl().nativeHandle
        val result = checkNotNull(gsl_matrix_float_calloc(a.pointed.size1, a.pointed.size2))
        gsl_blas_sgemm(CblasNoTrans, CblasNoTrans, 1f, x, a, 1f, result)
        return GslFloatMatrix(rawNativeHandle = result, scope = scope, owned = true)
    }

    override fun Matrix<Float>.dot(vector: Point<Float>): GslVector<Float, gsl_vector_float> {
        val x = toGsl().nativeHandle
        val a = vector.toGsl().nativeHandle
        val result = checkNotNull(gsl_vector_float_calloc(a.pointed.size))
        gsl_blas_sgemv(CblasNoTrans, 1f, x, a, 1f, result)
        return GslFloatVector(rawNativeHandle = result, scope = scope, owned = true)
    }

    override fun Matrix<Float>.times(value: Float): GslMatrix<Float, gsl_matrix_float> {
        val g1 = toGsl().copy()
        gsl_matrix_float_scale2(g1.nativeHandle, value)
        return g1
    }

    override fun Matrix<Float>.plus(other: Matrix<Float>): GslMatrix<Float, gsl_matrix_float> {
        val g1 = toGsl().copy()
        gsl_matrix_float_add(g1.nativeHandle, other.toGsl().nativeHandle)
        return g1
    }

    override fun Matrix<Float>.minus(other: Matrix<Float>): Matrix<Float> {
        val g1 = toGsl().copy()
        gsl_matrix_float_sub(g1.nativeHandle, other.toGsl().nativeHandle)
        return g1
    }
}

/**
 * Invokes [block] inside newly created [GslFloatLinearSpace] which is disposed when the block is invoked.
 */
public fun <R> GslFloatLinearSpace(block: GslFloatLinearSpace.() -> R): R =
    memScoped { GslFloatLinearSpace(this).block() }

/**
 * Represents [Complex] matrix context implementing where all the operations are delegated to GSL.
 */
@Suppress("OVERRIDE_BY_INLINE")
public class GslComplexLinearSpace(scope: AutofreeScope) :
    GslLinearSpace<Complex, ComplexField, gsl_matrix_complex, gsl_vector_complex>(scope) {
    override inline val elementAlgebra: ComplexField
        get() = ComplexField

    override fun produceDirtyMatrix(rows: Int, columns: Int): GslMatrix<Complex, gsl_matrix_complex> = GslComplexMatrix(
        rawNativeHandle = checkNotNull(gsl_matrix_complex_alloc(rows.toULong(), columns.toULong())),
        scope = scope,
        owned = true,
    )

    override fun produceDirtyVector(size: Int): GslVector<Complex, gsl_vector_complex> =
        GslComplexVector(
            rawNativeHandle = checkNotNull(gsl_vector_complex_alloc(size.toULong())),
            scope = scope,
            owned = true,
        )

    override fun Matrix<Complex>.dot(other: Matrix<Complex>): GslMatrix<Complex, gsl_matrix_complex> {
        val x = toGsl().nativeHandle
        val a = other.toGsl().nativeHandle
        val result = checkNotNull(gsl_matrix_complex_calloc(a.pointed.size1, a.pointed.size2))
        gsl_blas_zgemm(CblasNoTrans, CblasNoTrans, ComplexField.one.toGsl(), x, a, ComplexField.one.toGsl(), result)
        return GslComplexMatrix(rawNativeHandle = result, scope = scope, owned = true)
    }

    override fun Matrix<Complex>.dot(vector: Point<Complex>): GslVector<Complex, gsl_vector_complex> {
        val x = toGsl().nativeHandle
        val a = vector.toGsl().nativeHandle
        val result = checkNotNull(gsl_vector_complex_calloc(a.pointed.size))
        gsl_blas_zgemv(CblasNoTrans, ComplexField.one.toGsl(), x, a, ComplexField.one.toGsl(), result)
        return GslComplexVector(rawNativeHandle = result, scope = scope, owned = true)
    }

    override fun Matrix<Complex>.times(value: Complex): GslMatrix<Complex, gsl_matrix_complex> {
        val g1 = toGsl().copy()
        gsl_matrix_complex_scale(g1.nativeHandle, value.toGsl())
        return g1
    }

    override fun Matrix<Complex>.plus(other: Matrix<Complex>): GslMatrix<Complex, gsl_matrix_complex> {
        val g1 = toGsl().copy()
        gsl_matrix_complex_add(g1.nativeHandle, other.toGsl().nativeHandle)
        return g1
    }

    override fun Matrix<Complex>.minus(other: Matrix<Complex>): Matrix<Complex> {
        val g1 = toGsl().copy()
        gsl_matrix_complex_sub(g1.nativeHandle, other.toGsl().nativeHandle)
        return g1
    }

    @OptIn(PerformancePitfall::class)
    @Suppress("IMPLICIT_CAST_TO_ANY")
    @UnstableKMathAPI
    override fun <F : StructureFeature> getFeature(structure: Matrix<Complex>, type: KClass<out F>): F? =
        when (type) {
            LupDecompositionFeature::class, DeterminantFeature::class -> object : LupDecompositionFeature<Complex>,
                DeterminantFeature<Complex>, InverseMatrixFeature<Complex> {
                private val lups by lazy {
                    val lu = structure.toGsl().copy()
                    val n = structure.rowNum

                    val perm = GslPermutation(rawNativeHandle = checkNotNull(gsl_permutation_alloc(n.toULong())),
                        scope = scope,
                        owned = true)

                    val signum = memScoped {
                        val i = alloc<IntVar>()
                        gsl_linalg_complex_LU_decomp(lu.nativeHandle, perm.nativeHandle, i.ptr)
                        i.value
                    }

                    Triple(lu, perm, signum)
                }

                override val p by lazy {
                    val n = structure.rowNum
                    val one = buildMatrix(n, n) { i, j -> if (i == j) 1.0.toComplex() else 0.0.toComplex() }
                    val perm = buildMatrix(n, n) { _, _ -> 0.0.toComplex() }

                    for (j in 0 until lups.second.size)
                        gsl_matrix_complex_set_col(perm.nativeHandle,
                            j.toULong(),
                            one.columns[lups.second[j]].toGsl().nativeHandle)

                    perm
                }

                override val l by lazy {
                    VirtualMatrix(lups.first.shape[0], lups.first.shape[1]) { i, j ->
                        when {
                            j < i -> lups.first[i, j]
                            j == i -> 1.0.toComplex()
                            else -> 0.0.toComplex()
                        }
                    } + LFeature
                }

                override val u by lazy {
                    VirtualMatrix(
                        lups.first.shape[0],
                        lups.first.shape[1],
                    ) { i, j -> if (j >= i) lups.first[i, j] else 0.0.toComplex() } + UFeature
                }

                override val determinant by lazy {
                    gsl_linalg_complex_LU_det(lups.first.nativeHandle, lups.third).toKMath()
                }

                override val inverse by lazy {
                    val inv = lups.first.copy()
                    gsl_linalg_complex_LU_invx(inv.nativeHandle, lups.second.nativeHandle)
                    inv
                }
            }

            CholeskyDecompositionFeature::class -> object : CholeskyDecompositionFeature<Complex> {
                override val l by lazy {
                    val chol = structure.toGsl().copy()
                    gsl_linalg_complex_cholesky_decomp(chol.nativeHandle)
                    chol
                }
            }

            else -> super.getFeature(structure, type)
        }?.let(type::cast)
}

/**
 * Invokes [block] inside newly created [GslComplexLinearSpace] which is disposed when the block is invoked.
 */
public fun <R> GslComplexLinearSpace(block: GslComplexLinearSpace.() -> R): R =
    memScoped { GslComplexLinearSpace(this).block() }
