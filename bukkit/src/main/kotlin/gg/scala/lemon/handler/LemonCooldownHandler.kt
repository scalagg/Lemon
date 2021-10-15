package gg.scala.lemon.handler

import gg.scala.lemon.cooldown.CooldownHandler
import gg.scala.lemon.cooldown.impl.*

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
object LemonCooldownHandler
{

    fun initialLoad()
    {
        CooldownHandler.register(
            ChatCooldown, SlowChatCooldown,
            CommandCooldown, RequestCooldown,
            ReportCooldown
        )
    }
}
