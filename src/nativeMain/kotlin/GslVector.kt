package space.kscience.kmath.gsl

import kotlinx.cinterop.CStructVar
import space.kscience.kmath.structures.MutableBuffer

/**
 * Wraps gsl_vector_* objects from GSL.
 */
internal abstract class GslVector<T, H : CStructVar> internal constructor(scope: DeferScope, owned: Boolean) :
    GslObject<H>(scope, owned), MutableBuffer<T> {
    final override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var cursor = 0

        override fun hasNext(): Boolean = cursor < size

        override fun next(): T {
            cursor++
            return this@GslVector[cursor - 1]
        }
    }
}
