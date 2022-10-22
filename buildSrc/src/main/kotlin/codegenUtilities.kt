/*
 * Copyright 2021-2022 KMath contributors.
 * Use of this source code is governed by the GNU GPL v3 license that can be found in the license/LICENSE.txt file.
 */

package space.kscience.kmath.gsl.codegen

internal fun fn(pattern: String, type: String): String {
    if (type == "double") return pattern.replace("R", "_")
    return pattern.replace("R", "_${type}_")
}

internal fun sn(pattern: String, type: String): String {
    if (type == "double") return pattern.replace("R", "")
    return pattern.replace("R", "_$type")
}
