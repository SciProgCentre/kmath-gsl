package space.kscience.kmath.gsl.stat

import kotlinx.cinterop.*
import org.gnu.gsl.*
import space.kscience.kmath.gsl.GslObject
import space.kscience.kmath.stat.RandomGenerator

private const val POW_32 = 1L shl 32

/**
 * Partially derived from
 * [https://commons.apache.org/proper/commons-rng/commons-rng-core/apidocs/org/apache/commons/rng/core/source64/LongProvider.html].
 */
public class GslRandomGenerator internal constructor(
    override val rawNativeHandle: CPointer<gsl_rng>,
    public val type: Type,
    scope: AutofreeScope,
    owned: Boolean,
) : GslObject<gsl_rng>(scope, owned), RandomGenerator {
    public constructor(
        scope: AutofreeScope,
        type: Type = Type.default,
        seed: ULong? = null,
    ) : this(checkNotNull(gsl_rng_alloc(type.value)), type, scope, true) {
        if (seed != null) gsl_rng_set2(nativeHandle, seed)
    }

    public val name: String?
        get() = gsl_rng_name(nativeHandle)?.toKString()

    /**
     * Provides a bit source for booleans.
     *
     * A cached value from a call to [nextLong].
     */
    private var booleanSource: Long = 0L

    /**
     * The bit mask of the boolean source to obtain the boolean bit.
     *
     * The bit mask contains a single bit set. This begins at the least
     * significant bit and is gradually shifted upwards until overflow to zero.
     *
     * When zero a new boolean source should be created and the mask set to the
     * least significant bit (i.e., 1).
     */
    private var booleanBitMask: Long = 0L

    /**
     * Provides a source for ints.
     *
     * A cached value from a call to [nextLong].
     */
    private var intSource: Long = 0L

    /**
     * Flag to indicate an int source has been cached.
     */
    private var cachedIntSource: Boolean = false

    override fun close() = gsl_rng_free(nativeHandle)

    override fun fillBytes(array: ByteArray, fromIndex: Int, toIndex: Int) {
        val len = toIndex - fromIndex
        var max = array.size - 1

        if (fromIndex < 0 || fromIndex > max)
            throw IndexOutOfBoundsException("$fromIndex is out of interval [${0}, ${max}]")

        max = array.size - fromIndex

        if (len < 0 || len > max)
            throw IndexOutOfBoundsException("$len is out of interval [${0}, ${max}]")

        var index: Int = fromIndex // Index of first insertion.

        // Index of first insertion plus multiple of 8 part of length
        // (i.e., length with 3 least significant bits unset).
        val indexLoopLimit: Int = fromIndex + (len and 0x7ffffff8)

        // Start filling in the byte array, 8 bytes at a time.
        while (index < indexLoopLimit) {
            val random: Long = nextLong()
            array[index++] = random.toByte()
            array[index++] = (random ushr 8).toByte()
            array[index++] = (random ushr 16).toByte()
            array[index++] = (random ushr 24).toByte()
            array[index++] = (random ushr 32).toByte()
            array[index++] = (random ushr 40).toByte()
            array[index++] = (random ushr 48).toByte()
            array[index++] = (random ushr 56).toByte()
        }

        val indexLimit: Int = toIndex // Index of last insertion + 1.

        // Fill in the remaining bytes.
        if (index < indexLimit) {
            var random = nextLong()

            while (true) {
                array[index++] = random.toByte()

                random = if (index < indexLimit) {
                    random ushr 8
                } else {
                    break
                }
            }
        }
    }

    override fun fork(): RandomGenerator = GslRandomGenerator(scope, type, nextLong().toULong())

    override fun nextBoolean(): Boolean {
        // Shift up. This will eventually overflow and become zero.
        booleanBitMask = booleanBitMask shl 1

        // The mask will either contain a single bit or none.
        if (booleanBitMask == 0L) {
            // Set the least significant bit
            booleanBitMask = 1
            // Get the next value
            booleanSource = nextLong()
        }

        // Return if the bit is set
        return booleanSource and booleanBitMask != 0L
    }

    override fun nextDouble(): Double = gsl_rng_uniform(nativeHandle)

    override fun nextInt(): Int {
        // Directly store and use the long value as a source for ints
        if (cachedIntSource) {
            // Consume the cache value
            cachedIntSource = false
            // Return the lower 32 bits
            return intSource.toInt()
        }

        // Fill the cache
        cachedIntSource = true
        intSource = nextLong()

        // Return the upper 32 bits
        return (intSource ushr 32).toInt()
    }

    override fun nextInt(until: Int): Int {
        require(until > 0) { "Must be strictly positive: $until" }

        // Lemire (2019): Fast Random Integer Generation in an Interval
        // https://arxiv.org/abs/1805.10941

        var m: Long = (nextInt().toLong() and 0xffffffffL) * until
        var l = m and 0xffffffffL

        if (l < until) {
            // 2^32 % n
            val t: Long = POW_32 % until

            while (l < t) {
                m = (nextInt().toLong() and 0xffffffffL) * until
                l = m and 0xffffffffL
            }
        }

        return (m ushr 32).toInt()
    }

    override fun nextLong(): Long = gsl_rng_get(nativeHandle).toLong()

    override fun nextLong(until: Long): Long {
        require(until > 0) { "Must be strictly positive: $until" }

        var bits: Long
        var `val`: Long

        do {
            bits = nextLong() ushr 1
            `val` = bits % until
        } while (bits - `val` + (until - 1) < 0)

        return `val`
    }

    public enum class Type(internal val value: CPointer<gsl_rng_type>) {
        borosh13(gsl_rng_borosh13!!),
        coveyou(gsl_rng_coveyou!!),
        default(gsl_rng_default!!),
        fishman18(gsl_rng_fishman18!!),
        fishman20(gsl_rng_fishman20!!),
        fishman2x(gsl_rng_fishman2x!!),
        knuthran(gsl_rng_knuthran!!),
        knuthran2(gsl_rng_knuthran2!!),
        lecuyer21(gsl_rng_lecuyer21!!),
        minstd(gsl_rng_minstd!!),
        mrg(gsl_rng_mrg!!),
        mt19937(gsl_rng_mt19937!!),
        mt19937_1998(gsl_rng_mt19937_1998!!),
        mt19937_1999(gsl_rng_mt19937_1999!!),
        ran0(gsl_rng_ran0!!),
        ran2(gsl_rng_ran2!!),
        rand(gsl_rng_rand!!),
        rand48(gsl_rng_rand48!!),
        random128_bsd(gsl_rng_random128_bsd!!),
        random128_glibc2(gsl_rng_random128_glibc2!!),
        random128_libc5(gsl_rng_random128_libc5!!),
        random256_bsd(gsl_rng_random256_bsd!!),
        random256_glibc2(gsl_rng_random256_glibc2!!),
        random256_libc5(gsl_rng_random256_libc5!!),
        random32_glibc2(gsl_rng_random32_glibc2!!),
        random32_libc5(gsl_rng_random32_libc5!!),
        random64_bsd(gsl_rng_random64_bsd!!),
        random64_libc5(gsl_rng_random64_libc5!!),
        random8_bsd(gsl_rng_random8_bsd!!),
        random8_glibc2(gsl_rng_random8_glibc2!!),
        random8_libc5(gsl_rng_random8_libc5!!),
        random_bsd(gsl_rng_random_bsd!!),
        random_libc5(gsl_rng_random_libc5!!),
        ranf(gsl_rng_ranf!!),
        ranlux389(gsl_rng_ranlux389!!),
        ranlxs0(gsl_rng_ranlxs0!!),
        ranlxs1(gsl_rng_ranlxs1!!),
        ranlxs2(gsl_rng_ranlxs2!!),
        ranmar(gsl_rng_ranmar!!),
        slatec(gsl_rng_slatec!!),
        taus(gsl_rng_taus!!),
        taus113(gsl_rng_taus113!!),
        uni32(gsl_rng_uni32!!),
        vax(gsl_rng_vax!!),
        waterman14(gsl_rng_waterman14!!),
    }
}
