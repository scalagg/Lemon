package gg.scala.lemon.disguise.command

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
class DisguiseCheckCommand : BaseCommand() {

    @CommandAlias("dgc|checkdisguise")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.disguise.check")
    fun onCheckDisguise(player: Player, target: LemonPlayer) {
        if (target.bukkitPlayer!!.hasMetadata("disguised")) {
            player.sendMessage("${CC.YELLOW}${target.name}${CC.GREEN} is disguised as ${target.getColoredName()}${CC.GREEN}.")
        } else {
            player.sendMessage("${target.getColoredName()}'s${CC.RED} not disguised!")
        }
    }

    @CommandAlias("dgl|disguiselist")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.disguise.list")
    fun onDisguiseList(player: Player) {
        val disguised = Bukkit.getOnlinePlayers()
            .filter { it.hasMetadata("disguised") }

        if (disguised.isEmpty()) {
            throw ConditionFailedException("No online players are disguised.")
        }

        player.sendMessage("${CC.B_PRI}Online Disguised Players:")

        disguised.forEach {
            val lemonPlayer = PlayerHandler.findPlayer(it).orElse(null)

            if (lemonPlayer != null) {
                player.sendMessage("${CC.GRAY} - ${lemonPlayer.getColoredName()} ${CC.WHITE}(${lemonPlayer.name})")
            }
        }
    }
}