package gg.scala.lemon.disguise.command

import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
object DisguiseCommand : ScalaCommand()
{
    @CommandAlias("disguise")
    @CommandPermission("lemon.command.disguise")
    fun onDisguise(player: Player)
    {
        DisguiseProvider.handleRandomDisguise(player)
    }

    @CommandAlias("undisguise")
    @CommandPermission("lemon.command.disguise")
    fun onUnDisguise(player: Player)
    {
        DisguiseProvider.handleUnDisguise(player)
    }
}
