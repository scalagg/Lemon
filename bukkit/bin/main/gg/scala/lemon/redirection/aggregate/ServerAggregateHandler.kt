package gg.scala.lemon.redirection.aggregate

import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.lemon.redirection.PlayerRedirectSystem
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
    var servers = listOf<GameServer>()

    abstract fun group(): String
    abstract fun findBestChoice(player: Player): GameServer?

    @JvmOverloads
    fun redirect(
        metadata: (Player) -> Map<String, String> = { mapOf() },
        vararg player: Player
    )
    {
        val bestChoice = findBestChoice(player.first())
            ?: return kotlin.run {
                player.forEach {
                    it.sendMessage(
                        "${CC.RED}We could not find a server for you to join."
                    )
                }
            }

        for (other in player)
        {
            redirectSystem.redirect(
                other, bestChoice.id, metadata(other)
            )
        }
    }

    @JvmOverloads
    fun redirect(
        player: Player,
        metadata: Map<String, String> = mapOf()
    )
    {
        val bestChoice = findBestChoice(player)
            ?: return player.sendMessage(
                "${CC.RED}We could not find a server for you to join."
            )

        redirectSystem.redirect(
            player, bestChoice.id, metadata
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
        val instances = ServerContainer
            .getServersInGroup(this.group())
            .filterIsInstance<GameServer>()

        this.servers = instances
    }
}
