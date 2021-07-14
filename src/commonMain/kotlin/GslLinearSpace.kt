package space.kscience.kmath.gsl

import space.kscience.kmath.complex.Complex
import space.kscience.kmath.complex.ComplexField
import space.kscience.kmath.linear.LinearSpace
import space.kscience.kmath.linear.Matrix
import space.kscience.kmath.linear.Point
import space.kscience.kmath.misc.PerformancePitfall
import space.kscience.kmath.misc.UnstableKMathAPI
import space.kscience.kmath.nd.StructureFeature
import space.kscience.kmath.operations.DoubleField
import space.kscience.kmath.operations.FloatField
import space.kscience.kmath.operations.Ring
import kotlin.reflect.KClass

/**
 * Represents matrix context implementing where all the operations are delegated to GSL.
 */
public expect abstract class GslLinearSpace<T : Any, out A : Ring<T>> internal constructor() : LinearSpace<T, A> {
    /**
     * Converts this matrix to GSL one.
     */
    public abstract fun Matrix<T>.toGsl(): Matrix<T>

    /**
     * Converts this point to GSL one.
     */
    public abstract fun Point<T>.toGsl(): Point<T>
}


/**
 * Represents [Double] matrix context implementing where all the operations are delegated to GSL.
 */
public expect class GslDoubleLinearSpace(scope: DeferScope) :
    GslLinearSpace<Double, DoubleField> {
    public override val elementAlgebra: DoubleField

    public override fun Matrix<Double>.toGsl(): Matrix<Double>
    public override fun Point<Double>.toGsl(): Point<Double>

    public override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: DoubleField.(i: Int, j: Int) -> Double,
    ): Matrix<Double>

    public override fun buildVector(size: Int, initializer: DoubleField.(Int) -> Double): Point<Double>
    public override fun Matrix<Double>.dot(other: Matrix<Double>): Matrix<Double>
    public override fun Matrix<Double>.dot(vector: Point<Double>): Point<Double>
    public override fun Matrix<Double>.times(value: Double): Matrix<Double>
    public override fun Matrix<Double>.plus(other: Matrix<Double>): Matrix<Double>
    public override fun Matrix<Double>.minus(other: Matrix<Double>): Matrix<Double>

    @UnstableKMathAPI
    public fun <F : StructureFeature> getFeature(structure: Matrix<Double>, type: KClass<out F>): F?
}


/**
 * Invokes [block] inside newly created [GslDoubleLinearSpace] which is disposed when the block is invoked.
 */
public expect fun <R> GslDoubleLinearSpace(block: GslDoubleLinearSpace.() -> R): R

/**
 * Represents [Float] matrix context implementing where all the operations are delegated to GSL.
 */
public expect class GslFloatLinearSpace(scope: DeferScope) :
    GslLinearSpace<Float, FloatField> {
    public override val elementAlgebra: FloatField

    public override fun Matrix<Float>.toGsl(): Matrix<Float>
    public override fun Point<Float>.toGsl(): Point<Float>

    public override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: FloatField.(i: Int, j: Int) -> Float,
    ): Matrix<Float>

    public override fun buildVector(size: Int, initializer: FloatField.(Int) -> Float): Point<Float>
    public override fun Matrix<Float>.dot(other: Matrix<Float>): Matrix<Float>
    public override fun Matrix<Float>.dot(vector: Point<Float>): Point<Float>
    public override fun Matrix<Float>.times(value: Float): Matrix<Float>
    public override fun Matrix<Float>.plus(other: Matrix<Float>): Matrix<Float>
    public override fun Matrix<Float>.minus(other: Matrix<Float>): Matrix<Float>
}

/**
 * Invokes [block] inside newly created [GslFloatLinearSpace] which is disposed when the block is invoked.
 */
public expect fun <R> GslFloatLinearSpace(block: GslFloatLinearSpace.() -> R): R

/**
 * Represents [Complex] matrix context implementing where all the operations are delegated to GSL.
 */
public expect class GslComplexLinearSpace(scope: DeferScope) :
    GslLinearSpace<Complex, ComplexField> {
    public override val elementAlgebra: ComplexField

    public override fun Matrix<Complex>.toGsl(): Matrix<Complex>
    public override fun Point<Complex>.toGsl(): Point<Complex>

    public override fun buildMatrix(
        rows: Int,
        columns: Int,
        initializer: ComplexField.(i: Int, j: Int) -> Complex,
    ): Matrix<Complex>

    public override fun buildVector(size: Int, initializer: ComplexField.(Int) -> Complex): Point<Complex>
    public override fun Matrix<Complex>.dot(other: Matrix<Complex>): Matrix<Complex>
    public override fun Matrix<Complex>.dot(vector: Point<Complex>): Point<Complex>
    public override fun Matrix<Complex>.times(value: Complex): Matrix<Complex>
    public override fun Matrix<Complex>.plus(other: Matrix<Complex>): Matrix<Complex>
    public override fun Matrix<Complex>.minus(other: Matrix<Complex>): Matrix<Complex>

    @UnstableKMathAPI
    public fun <F : StructureFeature> getFeature(structure: Matrix<Complex>, type: KClass<out F>): F?
}

/**
 * Invokes [block] inside newly created [GslComplexLinearSpace] which is disposed when the block is invoked.
 */
public expect fun <R> GslComplexLinearSpace(block: GslComplexLinearSpace.() -> R): R

