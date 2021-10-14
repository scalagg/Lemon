package gg.scala.lemon.disguise.command

import gg.scala.lemon.disguise.DisguiseProvider
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
object DisguiseCommand : BaseCommand() {

    @CommandAlias("disguise")
    @CommandPermission("lemon.command.disguise")
    fun onDisguise(player: Player) {
        DisguiseProvider.handleRandomDisguise(player)
    }

    @CommandAlias("undisguise")
    @CommandPermission("lemon.command.disguise")
    fun onUnDisguise(player: Player) {
        DisguiseProvider.handleUnDisguise(player)
    }
}
