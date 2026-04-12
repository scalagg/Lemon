@file:JvmName("PlayerHandlerExtensions")
package gg.scala.lemon.handler

import gg.scala.lemon.player.LemonPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author Limo
 * @date 2/3/26
 */

fun PlayerHandler.getCorrectedPlayerList(
    sender: CommandSender,
    players: List<Player>
): Collection<LemonPlayer>
{
    return players.mapNotNull { player ->
        PlayerHandler.findPlayer(player.uniqueId).orElse(null)
    }
}
