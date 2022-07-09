package gg.scala.lemon.lease

/**
 * Invalidates leases based
 * on its dependency.
 *
 * @author GrowlyX
 * @since 6/26/2022
 */
object LeaseDependency
{
    val leases = mutableListOf<Lease<*>>()

    fun invalidate(
        dependency: Any
    )
    {
        leases
            .filter {
                it.dependencies
                    .contains(dependency)
            }
            .forEach {
                it.invalidate()
            }
    }
}
