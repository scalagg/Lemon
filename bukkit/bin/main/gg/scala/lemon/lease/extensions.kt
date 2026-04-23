package gg.scala.lemon.lease

import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

/**
 * Lease delegate tool.
 */
inline fun <reified T> lease(
    strategy: LeaseStrategy = LeaseStrategy.Compute,
    dependencies: List<Any> = listOf(),
    executor: Executor = ForkJoinPool.commonPool(),
    noinline compute: () -> T
): Lease<T> = Lease(
    compute, strategy,
    executor, dependencies
)
