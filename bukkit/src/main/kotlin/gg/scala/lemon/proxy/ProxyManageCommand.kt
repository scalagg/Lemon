package gg.scala.lemon.proxy

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer

/**
 * @author GrowlyX
 * @since 2/10/2025
 */
@AutoRegister
@CommandAlias("manageproxy")
@CommandPermission("lemon.command.manageproxy")
object ProxyManageCommand : ScalaCommand()
{
    @Default
    fun onDefault(player: ScalaPlayer) = ManageProxySettingsMenu().openMenu(player.bukkit())
}
