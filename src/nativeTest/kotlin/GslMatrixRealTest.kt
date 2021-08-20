package space.kscience.kmath.gsl

import space.kscience.kmath.gsl.linear.GslDoubleLinearSpace
import space.kscience.kmath.misc.PerformancePitfall
import space.kscience.kmath.structures.toList
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GslMatrixRealTest {
    @Test
    fun dimensions() = GslDoubleLinearSpace {
        val mat = buildMatrix(42, 24) { _, _ -> 0.0 }
        assertEquals(42, mat.rowNum)
        assertEquals(24, mat.colNum)
    }

    @Test
    fun get() = GslDoubleLinearSpace {
        val mat = buildMatrix(1, 1) { _, _ -> 42.0 }
        assertEquals(42.0, mat[0, 0])
    }

    @OptIn(PerformancePitfall::class)
    @Test
    fun rows() = GslDoubleLinearSpace {
        val mat = buildMatrix(2, 2) { i, j -> i.toDouble() + j }

        mat.rows.asIterable().zip(listOf(listOf(0.0, 1.0), listOf(1.0, 2.0))).forEach { (a, b) ->
            assertEquals(a.toList(), b)
        }
    }

    @OptIn(PerformancePitfall::class)
    @Test
    fun columns() = GslDoubleLinearSpace {
        val mat = buildMatrix(2, 2) { i, j -> i.toDouble() + j }

        mat.columns.asIterable().zip(listOf(listOf(0.0, 1.0), listOf(1.0, 2.0))).forEach { (a, b) ->
            assertEquals(a.toList(), b)
        }
    }
}
