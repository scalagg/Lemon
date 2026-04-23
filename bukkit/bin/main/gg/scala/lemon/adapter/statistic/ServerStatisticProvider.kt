package gg.scala.lemon.adapter.statistic

/**
 * @author GrowlyX
 * @since 10/8/2021
 */
interface ServerStatisticProvider
{
    fun ticksPerSecond(): Double
    fun cpuUsage(): Double
    fun memoryUsage(): Long
}
