package space.kscience.kmath.gsl

import space.kscience.kmath.distributions.NormalDistribution
import space.kscience.kmath.gsl.stat.GslNormalDistribution
import space.kscience.kmath.samplers.GaussianSampler
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GslNormalDistributionTest {
    @Test
    fun pdf() {
        assertEquals(
            NormalDistribution(GaussianSampler(2.0, 4.0)).probability(0.234),
            GslNormalDistribution(2.0, 4.0).probability(0.234),
            1e-15,
        )
    }

    @Test
    fun cdf() {
        assertEquals(
            NormalDistribution(GaussianSampler(2.0, 4.0)).cumulative(0.234),
            GslNormalDistribution(2.0, 4.0).cumulative(0.234),
            1e-15,
        )
    }
}
