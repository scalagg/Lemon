package gg.scala.lemon.command

import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.player.color.PlayerColorMenu
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
object ColorCommand : ScalaCommand()
{
    @CommandAlias("color|colors|colours|colour")
    @CommandPermission("lemon.command.color")
    fun onColor(player: Player)
    {
        PlayerColorMenu().openMenu(player)
    }
}
