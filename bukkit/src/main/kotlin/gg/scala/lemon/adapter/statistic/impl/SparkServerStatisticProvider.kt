package gg.scala.lemon.adapter.statistic.impl

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.Lemon
import gg.scala.lemon.adapter.statistic.ServerStatisticProvider
import java.lang.management.ManagementFactory

@Service
@IgnoreAutoScan
@SoftDependency(value = "spark")
class SparkServerStatisticProvider : ServerStatisticProvider
{
    companion object
    {
        private const val SPARK_PROVIDER = "me.lucko.spark.api.SparkProvider"
        private const val TPS_WINDOW = "me.lucko.spark.api.statistic.StatisticWindow\$TicksPerSecond"
        private const val CPU_WINDOW = "me.lucko.spark.api.statistic.StatisticWindow\$CpuUsage"
    }

    @Inject
    lateinit var plugin: Lemon

    @Configure
    fun configure()
    {
        this.plugin.serverStatisticProvider = this
    }

    override fun ticksPerSecond(): Double
    {
        val spark = spark() ?: throw RuntimeException("Spark is not available")
        val tps = spark.javaClass.getMethod("tps").invoke(spark)
        val windowClass = Class.forName(TPS_WINDOW)
        val seconds5 = java.lang.Enum.valueOf(windowClass.asSubclass(Enum::class.java) as Class<out Enum<*>>, "SECONDS_5")
        return (tps.javaClass.getMethod("poll", windowClass).invoke(tps, seconds5) as Number).toDouble()
    }

    override fun cpuUsage(): Double
    {
        val spark = spark() ?: throw RuntimeException("Spark is not available")
        val cpuProcess = spark.javaClass.getMethod("cpuProcess").invoke(spark)
        val windowClass = Class.forName(CPU_WINDOW)
        val seconds10 = java.lang.Enum.valueOf(windowClass.asSubclass(Enum::class.java) as Class<out Enum<*>>, "SECONDS_10")
        return (cpuProcess.javaClass.getMethod("poll", windowClass).invoke(cpuProcess, seconds10) as Number).toDouble()
    }

    override fun memoryUsage(): Long
    {
        return ManagementFactory.getMemoryMXBean().heapMemoryUsage.used
    }

    private fun spark(): Any?
    {
        val providerClass = Class.forName(SPARK_PROVIDER)
        return providerClass.getMethod("get").invoke(null)
    }
}
