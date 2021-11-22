/*
 * Copyright 2021 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl

import kotlinx.cinterop.AutofreeScope
import kotlinx.cinterop.CStructVar
import space.kscience.kmath.linear.Point

/**
 * Wraps gsl_vector_* objects from GSL.
 */
public abstract class GslVector<T, H : CStructVar> internal constructor(scope: AutofreeScope, owned: Boolean) :
    GslObject<H>(scope, owned), Point<T> {
    internal abstract operator fun set(index: Int, value: T)
    internal abstract fun copy(): GslVector<T, H>

    public final override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var cursor = 0

        override fun hasNext(): Boolean = cursor < size

        override fun next(): T {
            cursor++
            return this@GslVector[cursor - 1]
        }
    }
}
