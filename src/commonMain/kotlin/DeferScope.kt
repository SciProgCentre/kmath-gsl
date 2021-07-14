package space.kscience.kmath.gsl

public expect class DeferScope {
    public inline fun defer(crossinline block: () -> Unit)
}
