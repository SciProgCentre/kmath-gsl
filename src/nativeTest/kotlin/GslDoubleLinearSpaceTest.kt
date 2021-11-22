/*
 * Copyright 2021 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl

import space.kscience.kmath.linear.LinearSpace
import space.kscience.kmath.linear.Matrix
import space.kscience.kmath.linear.invoke
import space.kscience.kmath.misc.PerformancePitfall
import space.kscience.kmath.nd.StructureND
import space.kscience.kmath.structures.Buffer
import space.kscience.kmath.structures.DoubleBuffer
import space.kscience.kmath.structures.asSequence
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
        val res2 = LinearSpace.real { ma dot vb }
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
        val res2: Matrix<Double> = LinearSpace.real { ma dot mb }
        assertTrue(StructureND.contentEquals(res1, res2))
    }

    @OptIn(PerformancePitfall::class)
    @Test
    fun testManyCalls(): Unit = GslDoubleLinearSpace {
        val expected: Matrix<Double> = LinearSpace.real {
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

        // TODO replace with https://github.com/mipt-npm/kmath/issues/364
        expected.elements().forEach { (idx, i) -> assertEquals(i, prod[idx], 1e85) }
    }
}
