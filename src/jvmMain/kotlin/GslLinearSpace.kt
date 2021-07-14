package space.kscience.kmath.gsl

import space.kscience.kmath.complex.Complex
import space.kscience.kmath.complex.ComplexField
import space.kscience.kmath.linear.LinearSpace
import space.kscience.kmath.linear.Matrix
import space.kscience.kmath.linear.Point
import space.kscience.kmath.misc.UnstableKMathAPI
import space.kscience.kmath.nd.StructureFeature
import space.kscience.kmath.operations.DoubleField
import space.kscience.kmath.operations.FloatField
import space.kscience.kmath.operations.Ring
import kotlin.reflect.KClass

internal inline fun <T : Any, A : Ring<T>> GslMatrix<T>.fill(
    a: A,
    initializer: A.(Int, Int) -> T,
): GslMatrix<T> =
    apply {
        for (col in 0 until colNum) {
            for (row in 0 until rowNum) this[row, col] = a.initializer(row, col)
        }
    }

internal inline fun <T : Any, A : Ring<T>> GslVector<T>.fill(
    a: A,
    initializer: A.(Int) -> T,
): GslVector<T> =
    apply {
        for (index in 0 until size) this[index] = a.initializer(index)
    }


/**
 * Represents matrix context implementing where all the operations are delegated to GSL.
 */
public actual abstract class GslLinearSpace<T : Any, out A : Ring<T>> internal actual constructor() : LinearSpace<T, A> {
    /**
     * Converts this matrix to GSL one.
     */
    public actual abstract fun Matrix<T>.toGsl(): Matrix<T>

    /**
     * Converts this point to GSL one.
     */
    public actual abstract fun Point<T>.toGsl(): Point<T>
}


/**
 * Represents [Double] matrix context implementing where all the operations are delegated to GSL.
 */
public actual class GslDoubleLinearSpace actual constructor(scope: DeferScope) :
    GslLinearSpace<Double, DoubleField>() {
    public actual override val elementAlgebra: DoubleField get() = TODO()

    public actual override fun Matrix<Double>.toGsl(): Matrix<Double> = TODO()
    public actual override fun Point<Double>.toGsl(): Point<Double> = TODO()

    public actual override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: DoubleField.(i: Int, j: Int) -> Double,
    ): Matrix<Double> = TODO()

    public actual override fun buildVector(size: Int, initializer: DoubleField.(Int) -> Double): Point<Double> = TODO()
    public actual override fun Matrix<Double>.dot(other: Matrix<Double>): Matrix<Double> = TODO()
    public actual override fun Matrix<Double>.dot(vector: Point<Double>): Point<Double> = TODO()
    public actual override fun Matrix<Double>.times(value: Double): Matrix<Double> = TODO()
    public actual override fun Matrix<Double>.plus(other: Matrix<Double>): Matrix<Double> = TODO()
    public actual override fun Matrix<Double>.minus(other: Matrix<Double>): Matrix<Double> = TODO()

    @UnstableKMathAPI
    public actual override fun <F : StructureFeature> getFeature(structure: Matrix<Double>, type: KClass<out F>): F? =
        TODO()
}


/**
 * Invokes [block] inside newly created [GslDoubleLinearSpace] which is disposed when the block is invoked.
 */
public actual fun <R> GslDoubleLinearSpace(block: GslDoubleLinearSpace.() -> R): R = GslDoubleLinearSpace(DeferScope()).block()

/**
 * Represents [Float] matrix context implementing where all the operations are delegated to GSL.
 */
public actual class GslFloatLinearSpace actual constructor(scope: DeferScope) :
    GslLinearSpace<Float, FloatField>() {
    public actual override val elementAlgebra: FloatField get() = TODO()

    public actual override fun Matrix<Float>.toGsl(): Matrix<Float> = TODO()
    public actual override fun Point<Float>.toGsl(): Point<Float> = TODO()

    public actual override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: FloatField.(i: Int, j: Int) -> Float,
    ): Matrix<Float> = TODO()

    public actual override fun buildVector(size: Int, initializer: FloatField.(Int) -> Float): Point<Float> = TODO()
    public actual override fun Matrix<Float>.dot(other: Matrix<Float>): Matrix<Float> = TODO()
    public actual override fun Matrix<Float>.dot(vector: Point<Float>): Point<Float> = TODO()
    public actual override fun Matrix<Float>.times(value: Float): Matrix<Float> = TODO()
    public actual override fun Matrix<Float>.plus(other: Matrix<Float>): Matrix<Float> = TODO()
    public actual override fun Matrix<Float>.minus(other: Matrix<Float>): Matrix<Float> = TODO()
}

/**
 * Invokes [block] inside newly created [GslFloatLinearSpace] which is disposed when the block is invoked.
 */
public actual fun <R> GslFloatLinearSpace(block: GslFloatLinearSpace.() -> R): R = GslFloatLinearSpace(DeferScope()).block()

/**
 * Represents [Complex] matrix context implementing where all the operations are delegated to GSL.
 */
public actual class GslComplexLinearSpace actual constructor(scope: DeferScope) :
    GslLinearSpace<Complex, ComplexField>() {
    public actual override val elementAlgebra: ComplexField get() = TODO()

    public actual override fun Matrix<Complex>.toGsl(): Matrix<Complex> = TODO()
    public actual override fun Point<Complex>.toGsl(): Point<Complex> = TODO()

    public actual override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: ComplexField.(i: Int, j: Int) -> Complex,
    ): Matrix<Complex> = TODO()

    public actual override fun buildVector(size: Int, initializer: ComplexField.(Int) -> Complex): Point<Complex> =
        TODO()

    public actual override fun Matrix<Complex>.dot(other: Matrix<Complex>): Matrix<Complex> = TODO()
    public actual override fun Matrix<Complex>.dot(vector: Point<Complex>): Point<Complex> = TODO()
    public actual override fun Matrix<Complex>.times(value: Complex): Matrix<Complex> = TODO()
    public actual override fun Matrix<Complex>.plus(other: Matrix<Complex>): Matrix<Complex> = TODO()
    public actual override fun Matrix<Complex>.minus(other: Matrix<Complex>): Matrix<Complex> = TODO()

    @UnstableKMathAPI
    public actual override fun <F : StructureFeature> getFeature(structure: Matrix<Complex>, type: KClass<out F>): F? =
        TODO()
}

/**
 * Invokes [block] inside newly created [GslComplexLinearSpace] which is disposed when the block is invoked.
 */
public actual fun <R> GslComplexLinearSpace(block: GslComplexLinearSpace.() -> R): R =
    GslComplexLinearSpace(DeferScope()).block()
