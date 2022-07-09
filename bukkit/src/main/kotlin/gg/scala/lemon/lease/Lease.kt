package gg.scala.lemon.lease

import java.util.concurrent.Executor
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author GrowlyX
 * @since 6/26/2022
 */
class Lease<T>(
    private val compute: () -> T,
    private val strategy: LeaseStrategy,
    private val executor: Executor,
    val dependencies: List<Any>
) : ReadOnlyProperty<Any?, T?>
{
    private var value: T? = null
    private var valueExpired: T? = null

    private var computing = false

    init
    {
        LeaseDependency.leases.add(this)

        if (
            this.strategy == LeaseStrategy.ComputeEager ||
            this.strategy == LeaseStrategy.ExpiredEager
        )
        {
            runCatching {
                this.compute()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun invalidate()
    {
        this.valueExpired = this.value
        this.value = null

        if (this.strategy == LeaseStrategy.ExpiredEager)
        {
            asyncCompute()
        }
    }

    private fun compute(): T?
    {
        this.computing = true

        runCatching(compute)
            .onFailure {
                it.printStackTrace()
            }
            .onSuccess {
                this.value = it
            }

        this.computing = false
        return this.value
    }

    private fun asyncCompute()
    {
        executor
            .execute {
                compute()
            }
    }

    private fun value(): T?
    {
        if (this.value == null)
        {
            if (this.computing)
            {
                return this.valueExpired
            }

            return when (strategy)
            {
                LeaseStrategy.Compute, LeaseStrategy.ComputeEager -> this.compute()
                LeaseStrategy.Expired, LeaseStrategy.ExpiredEager ->
                {
                    this.asyncCompute()
                    this.valueExpired
                }
            }
        }

        return this.value
    }

    override fun getValue(
        thisRef: Any?, property: KProperty<*>
    ) = this.value()
}
