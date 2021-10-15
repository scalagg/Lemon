package gg.scala.lemon.cooldown.impl

import gg.scala.lemon.cooldown.type.PlayerCooldown
import gg.scala.lemon.cooldown.type.PlayerStaticCooldown
import gg.scala.lemon.handler.ChatHandler
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
object ChatCooldown : PlayerCooldown("chat") {

    override fun durationFor(t: Player): Long
    {
        val donor = t.hasPermission("lemon.donator")

        return if (donor)
        {
            1000L
        } else
        {
            3000L
        }
    }
}

object SlowChatCooldown : PlayerCooldown("slow-chat") {

    override fun durationFor(t: Player): Long
    {
        return ChatHandler.slowChatTime.toLong() * 1_000L
    }
}

object CommandCooldown : PlayerStaticCooldown(
    "command", 1000L
)
object RequestCooldown : PlayerStaticCooldown(
    "request", 60_000L
)
object ReportCooldown : PlayerStaticCooldown(
    "report", 60_000L
)
