package gg.scala.lemon.command.management

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.menu.server.ServerViewMenu

/**
 * @author Limo
 * @date 7/2/2025
 */
@AutoRegister
object ServersCommand : ScalaCommand() {

    @CommandAlias("servers")
    @Description("Manage servers")
    @CommandPermission("lemon.command.servers")
    fun onList(sender: ScalaPlayer) {
        ServerViewMenu().openMenu(sender.bukkit())
    }
}