package gg.scala.lemon.redirection.aggregate.impl

import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.lemon.redirection.PlayerRedirectSystem
import gg.scala.lemon.redirection.aggregate.ServerAggregateHandler
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import org.bukkit.entity.Player

class LeastTrafficServerAggregateHandler(
    private val group: String,
    redirectSystem: PlayerRedirectSystem<Player> = VelocityRedirectSystem
) : ServerAggregateHandler(redirectSystem)
{
    override fun group() = group

    override fun findBestChoice(player: Player): GameServer?
    {
        return servers
            .filter {
                it.getWhitelisted() == ServerSync.getLocalGameServer().getWhitelisted()
            }
            .minByOrNull {
                it.getPlayersCount() ?: Int.MAX_VALUE 
            }
    }
}
