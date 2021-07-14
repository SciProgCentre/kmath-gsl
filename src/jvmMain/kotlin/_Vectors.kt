package space.kscience.kmath.gsl

import jdk.incubator.foreign.MemoryAddress
import org.gnu.gsl.GSL.*
import org.gnu.gsl.gsl_vector

internal class GslDoubleVector(
    rawNativeHandle: MemoryAddress,
    scope: DeferScope,
    owned: Boolean,
) : GslVector<Double>(rawNativeHandle, scope, owned) {
    override val size: Int get() = gsl_vector.`size$get`(gsl_vector.ofAddress(rawNativeHandle, scope)).toInt()
    override operator fun get(index: Int): Double = gsl_vector_get(nativeHandle, index.toLong())
    override operator fun set(index: Int, value: Double): Unit = gsl_vector_set(nativeHandle, index.toLong(), value)

    override fun copy(): GslDoubleVector {
        val new = checkNotNull(gsl_vector_alloc(size.toLong()))
        gsl_vector_memcpy(new, nativeHandle)
        return GslDoubleVector(new, scope, true)
    }

    override fun close(): Unit = gsl_vector_free(nativeHandle)
}
