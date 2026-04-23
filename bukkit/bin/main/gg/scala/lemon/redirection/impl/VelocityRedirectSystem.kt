package gg.scala.lemon.redirection.impl

import gg.scala.commons.annotations.inject.AutoBind
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.redirection.PlayerRedirectSystem
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
@Service
@AutoBind
object VelocityRedirectSystem : PlayerRedirectSystem<Player>(
    VelocityRedirectContext, VelocityRedirectContext
)
{
    @Configure
    fun configureInjected() = configure()

    @Close
    fun closeInjected() = close()
}
