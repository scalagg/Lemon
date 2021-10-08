package gg.scala.lemon.adapt.statistic

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
