package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.PlayerHandler
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/3/2021
 */
class ModModeCommand : BaseCommand() {

    @CommandAlias("modmode|mm|h")
    @CommandPermission("lemon.command.modmode")
    fun onModMode(player: Player, @Optional target: OnlinePlayer?) {
        val toggling = target?.player ?: player

        if (target != player && !player.hasPermission("lemon.command.modemode.other")) {
            throw ConditionFailedException("You do not have permission to mod-mode others!")
        }

        if (toggling.hasMetadata("mod-mode")) {
            PlayerHandler.unModModePlayer(player, target?.player ?: player)
        } else {
            PlayerHandler.modModePlayer(player, target?.player ?: player)
        }
    }
}
