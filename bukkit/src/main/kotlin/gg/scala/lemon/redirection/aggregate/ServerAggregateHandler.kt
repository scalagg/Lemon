package gg.scala.lemon.redirection.aggregate

import gg.scala.lemon.handler.ServerHandler
import gg.scala.lemon.redirection.PlayerRedirectSystem
import gg.scala.lemon.server.ServerInstance
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
abstract class ServerAggregateHandler(
    private val redirectSystem: PlayerRedirectSystem<Player>
) : Runnable
{
   val servers = CopyOnWriteArrayList<ServerInstance>()

    abstract fun group(): String
    abstract fun findBestChoice(player: Player): ServerInstance?

    fun redirect(player: Player)
    {
        val bestChoice = findBestChoice(player)
            ?: return player.sendMessage(
                "${CC.RED}We could not find a server for you to join."
            )

        redirectSystem.redirect(
            player, bestChoice.serverId
        )
    }

    fun subscribe()
    {
        Schedulers.async()
            .runRepeating(
                this, 0L, 10L
            )
    }

    override fun run()
    {
        val instances = ServerHandler
            .fetchOnlineServerInstancesByGroup(group())
            .join()

        this.servers.addAll(instances.values)
    }
}
