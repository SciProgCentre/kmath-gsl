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
import space.kscience.kmath.operations.DoubleField
import space.kscience.kmath.operations.FloatField
import space.kscience.kmath.operations.Ring
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
public actual abstract class GslLinearSpace<T : Any, out A : Ring<T>> internal constructor(
    internal val scope: AutofreeScope,
) : AutofreeScope(), LinearSpace<T, A> {
    init {
        ensureHasGslErrorHandler()
    }

    /**
     * Converts this matrix to GSL one.
     */
    @Suppress("UNCHECKED_CAST")
    public actual abstract fun Matrix<T>.toGsl(): Matrix<T>

    /**
     * Converts this point to GSL one.
     */
    @Suppress("UNCHECKED_CAST")
    public actual abstract fun Point<T>.toGsl(): Point<T>

    override fun alloc(size: Long, align: Int): NativePointed = scope.alloc(size, align)
    override fun alloc(size: Int, align: Int): NativePointed = scope.alloc(size, align)
}

/**
 * Represents [Double] matrix context implementing where all the operations are delegated to GSL.
 */
public actual class GslDoubleLinearSpace actual constructor(scope: DeferScope) :
    GslLinearSpace<Double, DoubleField>(scope) {
    actual override val elementAlgebra: DoubleField
        get() = DoubleField

    private fun produceDirtyMatrix(rows: Int, columns: Int): GslMatrix<Double, gsl_matrix> = GslDoubleMatrix(
        rawNativeHandle = checkNotNull(gsl_matrix_alloc(rows.toULong(), columns.toULong())),
        scope = scope,
        owned = true,
    )

    private fun produceDirtyVector(size: Int): GslVector<Double, gsl_vector> =
        GslDoubleVector(rawNativeHandle = checkNotNull(gsl_vector_alloc(size.toULong())), scope = scope, owned = true)

    @Suppress("UNCHECKED_CAST")
    private fun Matrix<Double>.toGslInternal(): GslMatrix<Double, gsl_matrix> = (if (this is GslMatrix<*, *>)
        this
    else
        buildMatrix(rowNum, colNum) { i, j -> this@toGslInternal[i, j] }) as GslMatrix<Double, gsl_matrix>

    @Suppress("UNCHECKED_CAST")
    private fun Point<Double>.toGslInternal(): GslVector<Double, gsl_vector> =
        (if (this is GslVector<*, *>) this else produceDirtyVector(size).fill(
            elementAlgebra) { this@toGslInternal[it] }).copy() as GslVector<Double, gsl_vector>

    actual override fun Matrix<Double>.toGsl(): Matrix<Double> = toGslInternal()
    actual override fun Point<Double>.toGsl(): Point<Double> = toGslInternal()

    actual override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: DoubleField.(i: Int, j: Int) -> Double,
    ): Matrix<Double> = produceDirtyMatrix(rows, columns).fill(elementAlgebra, initializer)

    actual override fun buildVector(size: Int, initializer: DoubleField.(Int) -> Double): Point<Double> =
        produceDirtyVector(size).fill(elementAlgebra, initializer)

    actual override fun Matrix<Double>.dot(other: Matrix<Double>): Matrix<Double> {
        val x = toGslInternal().nativeHandle
        val a = other.toGslInternal().nativeHandle
        val result = checkNotNull(gsl_matrix_calloc(a.pointed.size1, a.pointed.size2))
        gsl_blas_dgemm(CblasNoTrans, CblasNoTrans, 1.0, x, a, 1.0, result)
        return GslDoubleMatrix(rawNativeHandle = result, scope = scope, owned = true)
    }

    actual override fun Matrix<Double>.dot(vector: Point<Double>): Point<Double> {
        val x = toGslInternal().nativeHandle
        val a = vector.toGslInternal().nativeHandle
        val result = checkNotNull(gsl_vector_calloc(a.pointed.size))
        gsl_blas_dgemv(CblasNoTrans, 1.0, x, a, 1.0, result)
        return GslDoubleVector(rawNativeHandle = result, scope = scope, owned = true)
    }

    actual override fun Matrix<Double>.times(value: Double): Matrix<Double> {
        val g1 = toGslInternal().copy()
        gsl_matrix_scale(g1.nativeHandle, value)
        return g1
    }

    actual override fun Matrix<Double>.plus(other: Matrix<Double>): Matrix<Double> {
        val g1 = toGslInternal().copy()
        gsl_matrix_add(g1.nativeHandle, other.toGslInternal().nativeHandle)
        return g1
    }

    actual override fun Matrix<Double>.minus(other: Matrix<Double>): Matrix<Double> {
        val g1 = toGslInternal().copy()
        gsl_matrix_sub(g1.nativeHandle, other.toGslInternal().nativeHandle)
        return g1
    }

    @OptIn(PerformancePitfall::class)
    @UnstableKMathAPI
    actual override fun <F : StructureFeature> getFeature(structure: Matrix<Double>, type: KClass<out F>): F? =
        when (type) {
            LupDecompositionFeature::class, DeterminantFeature::class -> object : LupDecompositionFeature<Double>,
                DeterminantFeature<Double>, InverseMatrixFeature<Double> {
                private val lups by lazy {
                    val lu = structure.toGslInternal().copy()
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
                    val perm = buildMatrix(n, n) { _, _ -> 0.0 }.toGslInternal()

                    for (j in 0 until lups.second.size)
                        gsl_matrix_set_col(perm.nativeHandle,
                            j.toULong(),
                            one.columns[lups.second[j]].toGslInternal().nativeHandle)

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
                    val chol = structure.toGslInternal().copy()
                    gsl_linalg_cholesky_decomp(chol.nativeHandle)
                    chol
                }
            }

            QRDecompositionFeature::class -> object : QRDecompositionFeature<Double> {
                private val qr by lazy {
                    val a = structure.toGslInternal().copy()
                    val q = buildMatrix(structure.rowNum, structure.rowNum) { _, _ -> 0.0 }.toGslInternal()
                    val r = buildMatrix(structure.rowNum, structure.colNum) { _, _ -> 0.0 }.toGslInternal()

                    if (structure.rowNum < structure.colNum) {
                        val tau = buildVector(min(structure.rowNum, structure.colNum)) { 0.0 }.toGslInternal()
                        gsl_linalg_QR_decomp(a.nativeHandle, tau.nativeHandle)
                        gsl_linalg_QR_unpack(a.nativeHandle, tau.nativeHandle, q.nativeHandle, r.nativeHandle)
                    } else {
                        val t = buildMatrix(structure.colNum, structure.colNum) { _, _ -> 0.0 }.toGslInternal()
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
public actual fun <R> GslDoubleLinearSpace(block: GslDoubleLinearSpace.() -> R): R =
    memScoped { GslDoubleLinearSpace(this).block() }

/**
 * Represents [Float] matrix context implementing where all the operations are delegated to GSL.
 */
public actual class GslFloatLinearSpace actual constructor(scope: DeferScope) :
    GslLinearSpace<Float, FloatField>(scope) {
    actual override val elementAlgebra: FloatField
        get() = FloatField

    private fun produceDirtyMatrix(rows: Int, columns: Int): GslMatrix<Float, gsl_matrix_float> =
        GslFloatMatrix(
            rawNativeHandle = checkNotNull(gsl_matrix_float_alloc(rows.toULong(), columns.toULong())),
            scope = scope,
            owned = true,
        )

    private fun produceDirtyVector(size: Int): GslVector<Float, gsl_vector_float> = GslFloatVector(
        rawNativeHandle = checkNotNull(value = gsl_vector_float_alloc(size.toULong())),
        scope = scope,
        owned = true,
    )

    @Suppress("UNCHECKED_CAST")
    private fun Matrix<Float>.toGslInternal(): GslMatrix<Float, gsl_matrix_float> = (if (this is GslMatrix<*, *>)
        this
    else
        buildMatrix(rowNum, colNum) { i, j -> this@toGslInternal[i, j] }) as GslMatrix<Float, gsl_matrix_float>

    @Suppress("UNCHECKED_CAST")
    private fun Point<Float>.toGslInternal(): GslVector<Float, gsl_vector_float> =
        (if (this is GslVector<*, *>) this else produceDirtyVector(size).fill(
            elementAlgebra) { this@toGslInternal[it] }).copy() as GslVector<Float, gsl_vector_float>

    actual override fun Matrix<Float>.toGsl(): Matrix<Float> = toGslInternal()
    actual override fun Point<Float>.toGsl(): Point<Float> = toGslInternal()

    actual override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: FloatField.(i: Int, j: Int) -> Float,
    ): Matrix<Float> = produceDirtyMatrix(rows, columns).fill(elementAlgebra, initializer)

    actual override fun buildVector(size: Int, initializer: FloatField.(Int) -> Float): Point<Float> =
        produceDirtyVector(size).fill(elementAlgebra, initializer)

    actual override fun Matrix<Float>.dot(other: Matrix<Float>): Matrix<Float> {
        val x = toGslInternal().nativeHandle
        val a = other.toGslInternal().nativeHandle
        val result = checkNotNull(gsl_matrix_float_calloc(a.pointed.size1, a.pointed.size2))
        gsl_blas_sgemm(CblasNoTrans, CblasNoTrans, 1f, x, a, 1f, result)
        return GslFloatMatrix(rawNativeHandle = result, scope = scope, owned = true)
    }

    actual override fun Matrix<Float>.dot(vector: Point<Float>): Point<Float> {
        val x = toGslInternal().nativeHandle
        val a = vector.toGslInternal().nativeHandle
        val result = checkNotNull(gsl_vector_float_calloc(a.pointed.size))
        gsl_blas_sgemv(CblasNoTrans, 1f, x, a, 1f, result)
        return GslFloatVector(rawNativeHandle = result, scope = scope, owned = true)
    }

    actual override fun Matrix<Float>.times(value: Float): Matrix<Float> {
        val g1 = toGslInternal().copy()
        gsl_matrix_float_scale2(g1.nativeHandle, value)
        return g1
    }

    actual override fun Matrix<Float>.plus(other: Matrix<Float>): Matrix<Float> {
        val g1 = toGslInternal().copy()
        gsl_matrix_float_add(g1.nativeHandle, other.toGslInternal().nativeHandle)
        return g1
    }

    actual override fun Matrix<Float>.minus(other: Matrix<Float>): Matrix<Float> {
        val g1 = toGslInternal().copy()
        gsl_matrix_float_sub(g1.nativeHandle, other.toGslInternal().nativeHandle)
        return g1
    }
}

/**
 * Invokes [block] inside newly created [GslFloatLinearSpace] which is disposed when the block is invoked.
 */
public actual fun <R> GslFloatLinearSpace(block: GslFloatLinearSpace.() -> R): R =
    memScoped { GslFloatLinearSpace(this).block() }

/**
 * Represents [Complex] matrix context implementing where all the operations are delegated to GSL.
 */
public actual class GslComplexLinearSpace actual constructor(scope: DeferScope) :
    GslLinearSpace<Complex, ComplexField>(scope) {
    actual override val elementAlgebra: ComplexField
        get() = ComplexField

    private fun produceDirtyMatrix(rows: Int, columns: Int): GslMatrix<Complex, gsl_matrix_complex> = GslComplexMatrix(
        rawNativeHandle = checkNotNull(gsl_matrix_complex_alloc(rows.toULong(), columns.toULong())),
        scope = scope,
        owned = true,
    )

    private fun produceDirtyVector(size: Int): GslVector<Complex, gsl_vector_complex> =
        GslComplexVector(
            rawNativeHandle = checkNotNull(gsl_vector_complex_alloc(size.toULong())),
            scope = scope,
            owned = true,
        )

    @Suppress("UNCHECKED_CAST")
    private fun Matrix<Complex>.toGslInternal(): GslMatrix<Complex, gsl_matrix_complex> = (if (this is GslMatrix<*, *>)
        this
    else
        buildMatrix(rowNum, colNum) { i, j -> this@toGslInternal[i, j] }) as GslMatrix<Complex, gsl_matrix_complex>

    @Suppress("UNCHECKED_CAST")
    private fun Point<Complex>.toGslInternal(): GslVector<Complex, gsl_vector_complex> =
        (if (this is GslVector<*, *>) this else produceDirtyVector(size).fill(
            elementAlgebra) { this@toGslInternal[it] }).copy() as GslVector<Complex, gsl_vector_complex>

    actual override fun Matrix<Complex>.toGsl(): Matrix<Complex> = toGslInternal()
    actual override fun Point<Complex>.toGsl(): Point<Complex> = toGslInternal()

    actual override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: ComplexField.(i: Int, j: Int) -> Complex,
    ): Matrix<Complex> = produceDirtyMatrix(rows, columns).fill(elementAlgebra, initializer)

    actual override fun buildVector(size: Int, initializer: ComplexField.(Int) -> Complex): Point<Complex> =
        produceDirtyVector(size).fill(elementAlgebra, initializer)

    actual override fun Matrix<Complex>.dot(other: Matrix<Complex>): Matrix<Complex> {
        val x = toGslInternal().nativeHandle
        val a = other.toGslInternal().nativeHandle
        val result = checkNotNull(gsl_matrix_complex_calloc(a.pointed.size1, a.pointed.size2))

        gsl_blas_zgemm(
            CblasNoTrans,
            CblasNoTrans,
            ComplexField.one.toGsl(),
            x,
            a,
            ComplexField.one.toGsl(),
            result,
        )

        return GslComplexMatrix(rawNativeHandle = result, scope = scope, owned = true)
    }

    actual override fun Matrix<Complex>.dot(vector: Point<Complex>): Point<Complex> {
        val x = toGslInternal().nativeHandle
        val a = vector.toGslInternal().nativeHandle
        val result = checkNotNull(gsl_vector_complex_calloc(a.pointed.size))
        gsl_blas_zgemv(CblasNoTrans, ComplexField.one.toGsl(), x, a, ComplexField.one.toGsl(), result)
        return GslComplexVector(rawNativeHandle = result, scope = scope, owned = true)
    }

    actual override fun Matrix<Complex>.times(value: Complex): Matrix<Complex> {
        val g1 = toGslInternal().copy()
        gsl_matrix_complex_scale(g1.nativeHandle, value.toGsl())
        return g1
    }

    actual override fun Matrix<Complex>.plus(other: Matrix<Complex>): Matrix<Complex> {
        val g1 = toGslInternal().copy()
        gsl_matrix_complex_add(g1.nativeHandle, other.toGslInternal().nativeHandle)
        return g1
    }

    actual override fun Matrix<Complex>.minus(other: Matrix<Complex>): Matrix<Complex> {
        val g1 = toGslInternal().copy()
        gsl_matrix_complex_sub(g1.nativeHandle, other.toGslInternal().nativeHandle)
        return g1
    }

    @OptIn(PerformancePitfall::class)
    @UnstableKMathAPI
    actual override fun <F : StructureFeature> getFeature(structure: Matrix<Complex>, type: KClass<out F>): F? =
        when (type) {
            LupDecompositionFeature::class, DeterminantFeature::class -> object : LupDecompositionFeature<Complex>,
                DeterminantFeature<Complex>, InverseMatrixFeature<Complex> {
                private val lups by lazy {
                    val lu = structure.toGslInternal().copy()
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
                    val perm = buildMatrix(n, n) { _, _ -> 0.0.toComplex() }.toGslInternal()

                    for (j in 0 until lups.second.size)
                        gsl_matrix_complex_set_col(perm.nativeHandle,
                            j.toULong(),
                            one.columns[lups.second[j]].toGslInternal().nativeHandle)

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
                    val chol = structure.toGslInternal().copy()
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
public actual fun <R> GslComplexLinearSpace(block: GslComplexLinearSpace.() -> R): R =
    memScoped { GslComplexLinearSpace(this).block() }
