package com.solexgames.lemon.command.moderation.punishment.debug

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/15/2021
 */
class PunishmentDebugCommand : BaseCommand() {

    @CommandAlias("punishment-debug")
    @CommandPermission("lemon.command.debug")
    fun onDebug(player: Player) {
        Lemon.instance.playerHandler.findPlayer(player).ifPresent {
            it.activePunishments.forEach { entry ->
                player.sendMessage("${CC.YELLOW}Value of ${CC.WHITE}${entry.key.name}${CC.YELLOW}: ${entry.value}")
            }
        }
    }
}
