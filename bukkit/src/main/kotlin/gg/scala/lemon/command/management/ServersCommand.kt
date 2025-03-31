package gg.scala.lemon.command.management

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.menu.server.ServerViewMenu

/**
 * Created by ScoreBoardAPI devscoreboardapi@gmail.com on 3/30/2025.
 */
@AutoRegister
object ServersCommand : ScalaCommand() {

    @CommandAlias("servers")
        fun onList(sender: ScalaPlayer) {
        ServerViewMenu().openMenu(sender.bukkit())
    }
}
