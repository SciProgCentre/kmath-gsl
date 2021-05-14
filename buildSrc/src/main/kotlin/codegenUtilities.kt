package space.kscience.kmath.gsl.codegen

internal fun fn(pattern: String, type: String): String {
    if (type == "double") return pattern.replace("R", "_")
    return pattern.replace("R", "_${type}_")
}

internal fun sn(pattern: String, type: String): String {
    if (type == "double") return pattern.replace("R", "")
    return pattern.replace("R", "_$type")
}
