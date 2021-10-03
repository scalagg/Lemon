package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.PlayerHandler
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/3/2021
 */
class ModModeCommand : BaseCommand() {

    @CommandAlias("modmode|mm|h")
    @CommandPermission("lemon.command.modmode")
    fun onModMode(player: Player) {
        if (player.hasMetadata("mod-mode")) {
            PlayerHandler.unModModePlayer(player)
        } else {
            PlayerHandler.modModePlayer(player)
        }
    }
}
