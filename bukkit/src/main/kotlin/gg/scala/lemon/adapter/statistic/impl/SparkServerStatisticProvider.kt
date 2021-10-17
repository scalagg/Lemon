package gg.scala.lemon.adapter.statistic.impl

import gg.scala.lemon.adapter.statistic.ServerStatisticProvider
import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import java.lang.management.ManagementFactory

/**
 * @author GrowlyX
 * @since 10/8/2021
 */
class SparkServerStatisticProvider : ServerStatisticProvider
{
    private val spark = SparkProvider.get()

    override fun ticksPerSecond(): Double = spark.tps()!!.poll(
        StatisticWindow.TicksPerSecond.SECONDS_5
    )

    override fun cpuUsage(): Double = spark.cpuProcess().poll(
        StatisticWindow.CpuUsage.SECONDS_10
    )

    override fun memoryUsage(): Long
    {
        return ManagementFactory.getMemoryMXBean().heapMemoryUsage.used
    }
}
