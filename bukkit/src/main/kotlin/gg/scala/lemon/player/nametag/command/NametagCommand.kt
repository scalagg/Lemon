package gg.scala.lemon.player.nametag.command

import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.nametag.rainbow.RainbowNametagHandler
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.annotation.Subcommand
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/25/2021
 */
@CommandAlias("nametag")
@CommandPermission("lemon.command.nametag")
object NametagCommand : BaseCommand()
{
    @Subcommand("rainbow")
    @CommandPermission("lemon.command.nametag.rainbow")
    fun onRainbow(player: Player, @Optional target: LemonPlayer?)
    {
        val uniqueId = target?.uniqueId ?: player.uniqueId

        if (RainbowNametagHandler.rainbowNametagEnabled.contains(uniqueId))
        {
            RainbowNametagHandler.rainbowNametagEnabled.remove(uniqueId)

            player.sendMessage("${CC.RED}Disabled rainbow nametag for: ${
                target?.getColoredName() ?: player.displayName
            }")

            NametagHandler.reloadPlayer(target?.bukkitPlayer ?: player)
        } else
        {
            RainbowNametagHandler.rainbowNametagEnabled.add(uniqueId)

            player.sendMessage("${CC.GREEN}Enabled rainbow nametag for: ${
                target?.getColoredName() ?: player.displayName
            }")
        }
    }
}