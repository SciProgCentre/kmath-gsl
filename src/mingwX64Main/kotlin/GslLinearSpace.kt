package space.kscience.kmath.gsl

import kotlinx.cinterop.CPointer
import org.gnu.gsl.gsl_matrix_float
import org.gnu.gsl.gsl_matrix_float_scale

internal actual fun callGslMatrixFloatScale(ptr: CPointer<gsl_matrix_float>, value: Float) {
    gsl_matrix_float_scale(ptr, value)
}
