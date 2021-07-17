package space.kscience.kmath.gsl.stat

import org.gnu.gsl.gsl_cdf_gaussian_P
import org.gnu.gsl.gsl_ran_gaussian
import org.gnu.gsl.gsl_ran_gaussian_pdf
import space.kscience.kmath.chains.Chain
import space.kscience.kmath.chains.SimpleChain
import space.kscience.kmath.distributions.UnivariateDistribution
import space.kscience.kmath.stat.RandomGenerator

public class GslNormalDistribution(public val mean: Double, public val standardDeviation: Double) :
    UnivariateDistribution<Double> {
    override fun probability(arg: Double): Double = gsl_ran_gaussian_pdf(arg - mean, standardDeviation)

    @Deprecated("Unsafe function.", level = DeprecationLevel.ERROR)
    override fun sample(generator: RandomGenerator): Chain<Double> {
        require(generator is GslRandomGenerator) { "Only GslRandomGenerator generators are supported" }
        return SimpleChain { gsl_ran_gaussian(generator.nativeHandle, standardDeviation) + mean }
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    public fun sample(generator: GslRandomGenerator): Chain<Double> = sample(generator as RandomGenerator)

    override fun cumulative(arg: Double): Double = gsl_cdf_gaussian_P(arg - mean, standardDeviation)
}
