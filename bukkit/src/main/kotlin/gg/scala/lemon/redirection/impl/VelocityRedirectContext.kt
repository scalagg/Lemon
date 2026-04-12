package gg.scala.lemon.redirection.impl

import gg.scala.lemon.Lemon
import gg.scala.lemon.redirection.*
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

object VelocityRedirectContext : PlayerRedirect<Player>, PlayerRedirectHandler
{
    override fun redirect(player: Player, server: PlayerRedirectMessage)
    {
        if (!Lemon.instance.settings.noNotifyRedirection)
        {
            player.sendMessage(
                "${CC.SEC}Joining ${CC.PRI}${server.server}${CC.SEC}..."
            )
        }

        BungeeUtil.sendToServer(player, server.server)
    }

    override fun retrieve(uniqueId: UUID): Player?
    {
        return Bukkit.getPlayer(uniqueId)
    }

    override fun retrieve(t: Player): UUID
    {
        return t.uniqueId
    }

    override fun sendMessage(
        player: Player, message: FancyMessage
    )
    {
        QuickAccess
            .sendGlobalPlayerFancyMessage(
                message, player.uniqueId
            )
    }

    override fun process(
        message: PlayerRedirectMessage
    ): CompletableFuture<PlayerRedirectionResponse>
    {
        return CompletableFuture.supplyAsync {

            

            

            val ticksPerSecond = Lemon.instance
                .serverStatisticProvider.ticksPerSecond()

            PlayerRedirectionResponse(
                ticksPerSecond >= 18.50,
                if (ticksPerSecond <= 18.50)
                {
                    "the server being temporarily paused"
                } else ""
            )
        }
    }
}
