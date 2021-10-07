package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/26/2021
 */
class PlaytimeCommand : BaseCommand() {

    @Syntax("<player>")
    @CommandAlias("playtime|pt")
    @CommandPermission("lemon.command.playtime")
    fun onPlayTime(
        player: Player, @Optional target: LemonPlayer?
    ) {
        val lemonPlayer = PlayerHandler.findPlayer(
            if (target == null) player else null
        ).orElse(null) ?: target

        if (target != null && !player.hasPermission("lemon.command.playtime.other")) {
            throw ConditionFailedException("You do not have permission to check the playtime of other players!")
        }

        val timeOnlineNetwork = lemonPlayer!!.pastLogins.values.sum()

        player.sendMessage("${ 
            if (target != null) {
                lemonPlayer.getColoredName() + "${CC.SEC} has"
            } else {
                "${CC.SEC}You've"
            }
        } played on the network for:")

        player.sendMessage("${CC.PRI}${
            DurationFormatUtils.formatDurationWords(
                timeOnlineNetwork, true, true
            )
        }${CC.SEC}")
    }
}
