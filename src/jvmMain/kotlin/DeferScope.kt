package space.kscience.kmath.gsl

import jdk.incubator.foreign.ResourceScope

public actual class DeferScope(internal val resource: ResourceScope = ResourceScope.newSharedScope()) : ResourceScope by resource {

    @PublishedApi
    internal var topDeferred: (() -> Unit)? = null

    init {
        defer(resource::close)
    }

    internal fun executeAllDeferred() {
        topDeferred?.let {
            it.invoke()
            topDeferred = null
        }
    }

    public actual inline fun defer(crossinline block: () -> Unit) {
        val currentTop = topDeferred
        topDeferred = {
            try {
                block()
            } finally {
                currentTop?.invoke()
            }
        }
    }
}
