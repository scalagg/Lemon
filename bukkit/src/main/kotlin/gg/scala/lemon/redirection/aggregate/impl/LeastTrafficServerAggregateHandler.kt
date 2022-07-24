package gg.scala.lemon.redirection.aggregate.impl

import gg.scala.lemon.redirection.PlayerRedirectSystem
import gg.scala.lemon.redirection.aggregate.ServerAggregateHandler
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.lemon.server.ServerInstance
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/24/2022
 */
class LeastTrafficServerAggregateHandler(
    private val group: String,
    redirectSystem: PlayerRedirectSystem<Player> = VelocityRedirectSystem
) : ServerAggregateHandler(redirectSystem)
{
    override fun group() = group

    override fun findBestChoice(player: Player): ServerInstance?
    {
        return servers
            .minByOrNull {
                it.onlinePlayers
            }
    }
}
