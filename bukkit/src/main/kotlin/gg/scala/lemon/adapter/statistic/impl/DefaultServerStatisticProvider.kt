package gg.scala.lemon.adapter.statistic.impl

import gg.scala.lemon.adapter.statistic.ServerStatisticProvider
import net.evilblock.cubed.util.nms.MinecraftReflection
import java.lang.management.ManagementFactory

object DefaultServerStatisticProvider : ServerStatisticProvider
{
    override fun ticksPerSecond(): Double
    {
        return MinecraftReflection.getTPS()
    }

    override fun cpuUsage(): Double
    {
        throw RuntimeException("DefaultSparkServerStatisticProvider#cpuUsage is not available")
    }

    override fun memoryUsage(): Long
    {
        return ManagementFactory.getMemoryMXBean().heapMemoryUsage.used
    }
}
