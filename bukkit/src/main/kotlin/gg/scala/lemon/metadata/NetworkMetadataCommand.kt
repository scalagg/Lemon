package gg.scala.lemon.metadata

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer

@AutoRegister
@CommandAlias("networkmetadata|netmeta|netmetadata")
@CommandPermission("lemon.command.networkmetadata")
object NetworkMetadataCommand : ScalaCommand()
{
    @Default
    fun default(player: ScalaPlayer) = NetworkMetadataEditMenu().openMenu(player.bukkit())
}
