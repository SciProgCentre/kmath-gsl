package space.kscience.kmath.gsl

import jdk.incubator.foreign.CLinker.toJavaString
import jdk.incubator.foreign.MemoryAddress
import org.gnu.gsl.GSL
import org.gnu.gsl.GSL.gsl_set_error_handler
import org.gnu.gsl.GSL.gsl_set_error_handler_off
import org.gnu.gsl.gsl_error_handler_t
import java.util.concurrent.atomic.AtomicBoolean

internal enum class GslErrnoValue(val code: Int, private val text: String) {
    GSL_SUCCESS(GSL.GSL_SUCCESS(), ""),
    GSL_FAILURE(GSL.GSL_FAILURE(), ""),
    GSL_CONTINUE(GSL.GSL_CONTINUE(), "iteration has not converged"),
    GSL_EDOM(GSL.GSL_EDOM(), "input domain error, e.g sqrt(-1)"),
    GSL_ERANGE(GSL.GSL_ERANGE(), "output range error, e.g. exp(1e100)"),
    GSL_EFAULT(GSL.GSL_EFAULT(), "invalid pointer"),
    GSL_EINVAL(GSL.GSL_EINVAL(), "invalid argument supplied by user"),
    GSL_EFAILED(GSL.GSL_EFAILED(), "generic failure"),
    GSL_EFACTOR(GSL.GSL_EFACTOR(), "factorization failed"),
    GSL_ESANITY(GSL.GSL_ESANITY(), "sanity check failed - shouldn't happen"),
    GSL_ENOMEM(GSL.GSL_ENOMEM(), "malloc failed"),
    GSL_EBADFUNC(GSL.GSL_EBADFUNC(), "problem with user-supplied function"),
    GSL_ERUNAWAY(GSL.GSL_ERUNAWAY(), "iterative process is out of control"),
    GSL_EMAXITER(GSL.GSL_EMAXITER(), "exceeded max number of iterations"),
    GSL_EZERODIV(GSL.GSL_EZERODIV(), "tried to divide by zero"),
    GSL_EBADTOL(GSL.GSL_EBADTOL(), "user specified an invalid tolerance"),
    GSL_ETOL(GSL.GSL_ETOL(), "failed to reach the specified tolerance"),
    GSL_EUNDRFLW(GSL.GSL_EUNDRFLW(), "underflow"),
    GSL_EOVRFLW(GSL.GSL_EOVRFLW(), "overflow"),
    GSL_ELOSS(GSL.GSL_ELOSS(), "loss of accuracy"),
    GSL_EROUND(GSL.GSL_EROUND(), "failed because of roundoff error"),
    GSL_EBADLEN(GSL.GSL_EBADLEN(), "matrix, vector lengths are not conformant"),
    GSL_ENOTSQR(GSL.GSL_ENOTSQR(), "matrix not square"),
    GSL_ESING(GSL.GSL_ESING(), "apparent singularity detected"),
    GSL_EDIVERGE(GSL.GSL_EDIVERGE(), "integral or series is divergent"),
    GSL_EUNSUP(GSL.GSL_EUNSUP(), "requested feature is not supported by the hardware"),
    GSL_EUNIMPL(GSL.GSL_EUNIMPL(), "requested feature not (yet) implemented"),
    GSL_ECACHE(GSL.GSL_ECACHE(), "cache limit exceeded"),
    GSL_ETABLE(GSL.GSL_ETABLE(), "table limit exceeded"),
    GSL_ENOPROG(GSL.GSL_ENOPROG(), "iteration is not making progress towards solution"),
    GSL_ENOPROGJ(GSL.GSL_ENOPROGJ(), "jacobian evaluations are not improving the solution"),
    GSL_ETOLF(GSL.GSL_ETOLF(), "cannot reach the specified tolerance in F"),
    GSL_ETOLX(GSL.GSL_ETOLX(), "cannot reach the specified tolerance in X"),
    GSL_ETOLG(GSL.GSL_ETOLG(), "cannot reach the specified tolerance in gradient"),
    GSL_EOF(GSL.GSL_EOF(), "end of file");

    override fun toString(): String = "${name}('$text')"

    companion object {
        fun valueOf(code: Int): GslErrnoValue? = values().find { it.code == code }
    }
}

/**
 * Wraps all the errors reported by GSL.
 */
public class GslException internal constructor(file: String, line: Int, reason: String, errno: Int) :
    RuntimeException("$file:$line: $reason. errno - $errno, ${GslErrnoValue.valueOf(errno)}")

private val isKmathHandlerRegistered = AtomicBoolean(false)

internal fun checkNotNullAddress(address: MemoryAddress): MemoryAddress {
    check(address !== MemoryAddress.NULL) { "Required value was null." }
    return address
}

internal fun ensureHasGslErrorHandler() {
    if (isKmathHandlerRegistered.get()) return
    gsl_set_error_handler_off()

    gsl_set_error_handler(gsl_error_handler_t.allocate { reason, file, line, errno ->
        throw GslException(
            toJavaString(checkNotNullAddress(file)),
            line,
            toJavaString(checkNotNullAddress(reason)),
            errno,
        )
    })

    isKmathHandlerRegistered.set(true)
}
