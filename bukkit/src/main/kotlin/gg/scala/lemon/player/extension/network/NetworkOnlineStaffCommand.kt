package gg.scala.lemon.player.extension.network

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/22/2021
 */
@AutoRegister
object NetworkOnlineStaffCommand : ScalaCommand()
{
    @CommandPermission("lemon.staff")
    @CommandAlias("onlinestaff|os|stafflist")
    fun onOnlineStaff(player: Player)
    {
        NetworkOnlineStaffMenu().openMenu(player)
    }
}
