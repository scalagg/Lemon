package gg.scala.lemon.filter.ml

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer

/**
 * @author GrowlyX
 * @since 8/21/2024
 */
@AutoRegister
@CommandAlias("chatml|cml")
@CommandPermission("lemon.command.chatml")
object ChatMLCommand : ScalaCommand()
{
    @Default
    fun default(player: ScalaPlayer) = ChatMLEditMenu().openMenu(player.bukkit())
}
