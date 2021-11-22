/*
 * Copyright 2021 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl

import kotlinx.cinterop.*
import org.gnu.gsl.*

internal class GslDoubleVector(
    override val rawNativeHandle: CPointer<gsl_vector>, 
    scope: AutofreeScope, 
    owned: Boolean,
) : GslVector<Double, gsl_vector>(scope, owned) {
    override val size: Int get() = nativeHandle.pointed.size.toInt()
    override operator fun get(index: Int): Double = gsl_vector_get(nativeHandle, index.toULong())
    override operator fun set(index: Int, value: Double): Unit = gsl_vector_set(nativeHandle, index.toULong(), value)

    override fun copy(): GslDoubleVector {
        val new = checkNotNull(gsl_vector_alloc(size.toULong()))
        gsl_vector_memcpy(new, nativeHandle)
        return GslDoubleVector(new, scope, true)
    }

    override fun close(): Unit = gsl_vector_free(nativeHandle)
}

internal class GslFloatVector(
    override val rawNativeHandle: CPointer<gsl_vector_float>, 
    scope: AutofreeScope, 
    owned: Boolean,
) : GslVector<Float, gsl_vector_float>(scope, owned) {
    override val size: Int get() = nativeHandle.pointed.size.toInt()
    override operator fun get(index: Int): Float = gsl_vector_float_get(nativeHandle, index.toULong())
    override operator fun set(index: Int, value: Float): Unit = gsl_vector_float_set(nativeHandle, index.toULong(), value)

    override fun copy(): GslFloatVector {
        val new = checkNotNull(gsl_vector_float_alloc(size.toULong()))
        gsl_vector_float_memcpy(new, nativeHandle)
        return GslFloatVector(new, scope, true)
    }

    override fun close(): Unit = gsl_vector_float_free(nativeHandle)
}

