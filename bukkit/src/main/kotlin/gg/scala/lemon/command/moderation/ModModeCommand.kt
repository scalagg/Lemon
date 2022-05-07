package gg.scala.lemon.command.moderation

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.acf.bukkit.contexts.OnlinePlayer
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
