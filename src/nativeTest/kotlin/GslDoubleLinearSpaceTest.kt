/*
 * Copyright 2021-2022 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl

import space.kscience.kmath.linear.Matrix
import space.kscience.kmath.linear.invoke
import space.kscience.kmath.linear.linearSpace
import space.kscience.kmath.misc.PerformancePitfall
import space.kscience.kmath.nd.StructureND
import space.kscience.kmath.operations.DoubleField
import space.kscience.kmath.operations.asSequence
import space.kscience.kmath.structures.Buffer
import space.kscience.kmath.structures.DoubleBuffer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTime

internal class GslDoubleLinearSpaceTest {
    @Test
    fun testScale() = GslDoubleLinearSpace {
        val ma = buildMatrix(10, 10) { _, _ -> 0.1 }
        val mb = ma * 20.0
        assertEquals(mb[0, 1], 2.0)
    }

    @Test
    fun testDotOfMatrixAndVector() = GslDoubleLinearSpace {
        val ma = buildMatrix(2, 2) { _, _ -> 100.0 }
        val vb = DoubleBuffer(2) { 0.1 }
        val res1 = ma dot vb
        val res2 = DoubleField.linearSpace { ma dot vb }
        println(res1.asSequence().toList())
        println(res2.asSequence().toList())
        assertTrue(Buffer.contentEquals(res1, res2))
    }

    @OptIn(PerformancePitfall::class)
    @Test
    fun testDotOfMatrixAndMatrix() = GslDoubleLinearSpace {
        val ma = buildMatrix(2, 2) { _, _ -> 100.0 }
        val mb = buildMatrix(2, 2) { _, _ -> 100.0 }
        val res1: Matrix<Double> = ma dot mb
        val res2: Matrix<Double> = DoubleField.linearSpace { ma dot mb }
        assertTrue(StructureND.contentEquals(res1, res2))
    }

    /**
     * Compares computation time of A^100, where A is 20x20 matrix filled with doubles returned by [Random] with seed
     * `0`.
     */
    @Test
    fun testManyCalls(): Unit = GslDoubleLinearSpace {
        DoubleField.linearSpace {
            val rng = Random(0)
            var prod = buildMatrix(20, 20) { _, _ -> rng.nextDouble() }
            val mult = buildMatrix(20, 20) { _, _ -> rng.nextDouble() }

            measureTime {
                repeat(100) { prod = prod dot mult }
            }.also(::println)

            prod
        }

        val rng = Random(0)
        var prod: Matrix<Double> = buildMatrix(20, 20) { _, _ -> rng.nextDouble() }
        val mult = buildMatrix(20, 20) { _, _ -> rng.nextDouble() }

        measureTime {
            repeat(100) { prod = prod dot mult }
        }.also(::println)
    }
}
