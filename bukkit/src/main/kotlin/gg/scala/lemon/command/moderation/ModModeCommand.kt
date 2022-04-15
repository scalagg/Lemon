package gg.scala.lemon.command.moderation

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/3/2021
 */
@AutoRegister
object ModModeCommand : ScalaCommand()
{
    @CommandAlias("modmode|mod|mm|h")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.modmode")
    fun onModMode(player: Player, @Optional target: LemonPlayer?)
    {
        val toggling = target?.bukkitPlayer ?: player

        if (target != null && target.bukkitPlayer != player && !player.hasPermission("lemon.command.modmode.other"))
        {
            throw ConditionFailedException("You do not have permission to mod-mode others!")
        }

        if (toggling.hasMetadata("mod-mode"))
        {
            PlayerHandler.unModModePlayer(player, target?.bukkitPlayer ?: player)
        } else
        {
            PlayerHandler.modModePlayer(player, target?.bukkitPlayer ?: player)
        }
    }
}
