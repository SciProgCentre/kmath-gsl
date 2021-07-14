package space.kscience.kmath.gsl

import jdk.incubator.foreign.MemoryAddress
import org.gnu.gsl.GSL_2.gsl_permutation_free
import org.gnu.gsl.GSL_2.gsl_permutation_get
import org.gnu.gsl.gsl_permutation

internal class GslPermutation(
    rawNativeHandle: MemoryAddress,
    scope: DeferScope,
    owned: Boolean,
) : GslObject(rawNativeHandle, scope, owned) {
    val size
        get() = gsl_permutation.`size$get`(gsl_permutation.ofAddress(nativeHandle, scope))

    operator fun get(i: Int) = gsl_permutation_get(nativeHandle, i.toLong()).toInt()
    override fun close() = gsl_permutation_free(nativeHandle)
}
