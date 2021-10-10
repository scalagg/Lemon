package gg.scala.lemon.task

import gg.scala.lemon.Lemon
import gg.scala.lemon.adapt.statistic.ServerStatisticProvider
import me.lucko.helper.Commands
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 10/10/2021
 */
class ServerMonitorRunnable(
    private val statisticProvider: ServerStatisticProvider
) : Runnable
{

    override fun run()
    {
        val belowConsideredHigh = Bukkit.getOnlinePlayers().size < 200

        val cpuUsage = statisticProvider.cpuUsage()
        val memoryUsage = (statisticProvider.memoryUsage() / 1024) / 1000
        val ticksPerSecond = statisticProvider.ticksPerSecond()

        val failedCpu = belowConsideredHigh && cpuUsage > 20.0
        val failedMemory = belowConsideredHigh && memoryUsage > 1000.0
        val failedTps = belowConsideredHigh && ticksPerSecond < 19.4

        val formatted = " ${CC.GRAY}(${
            String.format("%.2f", 
                cpuUsage.coerceAtMost(20.0)
            )
        }%) (${
            String.format("%,d",
                memoryUsage
            )
        }mb) (${
            String.format("%.2f",
                ticksPerSecond.coerceAtMost(20.0)
            )
        } tps)"

        Bukkit.getOnlinePlayers()
            .filter { it.hasPermission("lemon.monitor.alerts") }
            .forEach {
                it.sendMessage(
                    if (failedMemory || failedCpu || failedTps)
                    {
                        "${CC.D_RED} ✘ ${CC.RED}Failed server check!$formatted"
                    } else
                    {
                        "${CC.D_GREEN} ✔ ${CC.GREEN}Passed server check.$formatted"
                    }
                )
            }
    }
}
