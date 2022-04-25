package gg.scala.lemon.adapter.statistic.impl

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.adapter.statistic.ServerStatisticProvider
import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import java.lang.management.ManagementFactory

/**
 * @author GrowlyX
 * @since 10/8/2021
 */
@Service
@SoftDependency("spark")
class SparkServerStatisticProvider : ServerStatisticProvider
{
    @Inject
    lateinit var plugin: Lemon

    @Configure
    fun configure()
    {
        this.plugin.serverStatisticProvider = this
    }

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
