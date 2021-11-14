package gg.scala.lemon.command

import gg.scala.lemon.annotation.DoNotRegister
import gg.scala.lemon.player.color.PlayerColorMenu
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
@DoNotRegister
class ColorCommand : BaseCommand()
{
    @CommandAlias("color|colors|colours|colour")
    @CommandPermission("lemon.command.color")
    fun onColor(player: Player)
    {
        PlayerColorMenu().openMenu(player)
    }
}
