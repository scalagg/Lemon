package gg.scala.lemon.aggregate

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * @author GrowlyX
 * @since 7/9/2022
 */
object ServerGroupAggregates : ScheduledExecutorService by Executors.newSingleThreadScheduledExecutor()
