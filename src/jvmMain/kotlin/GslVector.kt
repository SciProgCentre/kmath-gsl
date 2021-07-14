package space.kscience.kmath.gsl

import jdk.incubator.foreign.MemoryAddress
import space.kscience.kmath.linear.Point
import space.kscience.kmath.structures.MutableBuffer

/**
 * Wraps gsl_vector_* objects from GSL.
 */
internal abstract class GslVector<T> internal constructor(
    rawNativeHandle: MemoryAddress,
    scope: DeferScope,
    owned: Boolean,
) : GslObject(rawNativeHandle, scope, owned), MutableBuffer<T> {
    final override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var cursor = 0

        override fun hasNext(): Boolean = cursor < size

        override fun next(): T {
            cursor++
            return this@GslVector[cursor - 1]
        }
    }
}
