package gg.scala.lemon.lease

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
