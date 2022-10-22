/*
 * Copyright 2021-2022 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl

import kotlinx.cinterop.AutofreeScope
import kotlinx.cinterop.CStructVar
import space.kscience.kmath.linear.Matrix
import space.kscience.kmath.nd.StructureND

/**
 * Wraps gsl_matrix_* objects from GSL.
 */
public abstract class GslMatrix<T : Any, H : CStructVar> internal constructor(scope: AutofreeScope, owned: Boolean) :
    GslObject<H>(scope, owned), Matrix<T> {
    internal abstract operator fun set(i: Int, j: Int, value: T)
    internal abstract fun copy(): GslMatrix<T, H>

    override fun toString(): String = StructureND.toString(this)
}
