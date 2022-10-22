/*
 * Copyright 2021-2022 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl

import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class ErrorsHandlingTest {
    @Test
    fun matrixAllocation() {
        assertFailsWith<GslException> {
            GslDoubleLinearSpace {
                buildMatrix(Int.MAX_VALUE, Int.MAX_VALUE) { _, _ -> 0.0 }
            }
        }
    }

    @Test
    fun useOfClosedObject() {
        val mat = GslDoubleLinearSpace {
            buildMatrix(1, 1) { _, _ -> 0.0 }
        }

        assertFailsWith<IllegalStateException> { mat.colNum }
        assertFailsWith<IllegalStateException> { mat.rowNum }
        assertFailsWith<IllegalStateException> { mat[0, 0] }
    }
}
