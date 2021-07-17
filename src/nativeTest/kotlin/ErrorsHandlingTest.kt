package space.kscience.kmath.gsl

import space.kscience.kmath.gsl.linear.GslDoubleLinearSpace
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
