package gg.scala.lemon.handler

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.cooldown.CooldownHandler
import gg.scala.lemon.cooldown.impl.*

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
@Service
object LemonCooldownHandler
{
    @Configure
    fun initialLoad()
    {
        CooldownHandler.register(
            ChatCooldown, SlowChatCooldown,
            CommandCooldown, RequestCooldown,
            ReportCooldown
        )
    }
}
