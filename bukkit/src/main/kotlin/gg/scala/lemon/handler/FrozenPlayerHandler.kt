package gg.scala.lemon.handler

import gg.scala.lemon.Lemon
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
import gg.scala.lemon.util.other.Expirable
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
object FrozenPlayerHandler : Runnable
{
    val expirables = mutableMapOf<UUID, FrozenExpirable>()

    override fun run()
    {
        Bukkit.getOnlinePlayers()
            .filter { it.hasMetadata("frozen") }
            .forEach {
                val expirable = expirables[it.uniqueId]

                if (expirable == null)
                {
                    it.sendMessage(
                        Lemon.instance.languageConfig.frozenPlayerTimeIsUpMessage
                    )
                } else
                {
                    it.sendMessage(
                        Lemon.instance.languageConfig.frozenPlayerHasTimeMessage
                            .replace("%s", expirable.durationFromNowStringRaw)
                    )
                }
            }
    }

    class FrozenExpirable : Expirable(
        System.currentTimeMillis(),
        TimeUnit.MINUTES.toMillis(5)
    )

    class FrozenPlayerTick : Runnable
    {
        override fun run()
        {
            val finalMap = hashMapOf<UUID, FrozenExpirable>().also {
                expirables.forEach { entry ->
                    it[entry.key] = entry.value
                }
            }

            finalMap.forEach { (uuid, expirable) ->
                if (expirable.hasExpired)
                {
                    sendStaffMessage(
                        null, "${CC.AQUA}${coloredName(uuid)}${CC.D_AQUA} has been frozen for 5 minutes.",
                        true, QuickAccess.MessageType.NOTIFICATION
                    )

                    expirables.remove(uuid)
                }
            }
        }
    }
}
