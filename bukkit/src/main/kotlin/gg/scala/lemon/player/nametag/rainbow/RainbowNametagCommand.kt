package gg.scala.lemon.player.nametag.rainbow

import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/25/2021
 */
object RainbowNametagCommand : BaseCommand()
{
    @CommandAlias("nametag rainbow")
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
        } else
        {
            RainbowNametagHandler.rainbowNametagEnabled.add(uniqueId)

            player.sendMessage("${CC.GREEN}Enabled rainbow nametag for: ${
                target?.getColoredName() ?: player.displayName
            }")
        }
    }
}