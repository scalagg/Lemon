package gg.scala.lemon.player.extension.network

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
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
