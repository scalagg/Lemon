@file:JvmName("PlayerHandlerExtensions")
package gg.scala.lemon.handler

import gg.scala.lemon.player.LemonPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Extension functions for PlayerHandler to support additional overloads.
 *
 * @author Limo
 * @date 2/3/26
 */

/**
 * Get corrected player list from a specific list of players.
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
