package gg.scala.lemon.player.entity.superboat

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/25/2021
 */
@AutoRegister
@CommandAlias("superboat")
@CommandPermission("lemon.command.superboat")
object EntitySuperBoatCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("forget")
    @Description("Destroy the current superboat shown to a player.")
    fun onDelete(player: Player, target: OnlinePlayer)
    {
        if (!EntitySuperBoatHandler.hasSuperBoat(target.player))
        {
            throw ConditionFailedException("That player does not have a superboat.")
        }

        EntitySuperBoatHandler.destroySuperBoatOf(target.player)

        player.sendMessage("${CC.GREEN}Successfully destroyed superboat.")
    }

    @Subcommand("create")
    @Description("Create a new superboat (lagger) for a player.")
    fun onCreate(player: Player, target: OnlinePlayer, size: Int)
    {
        if (EntitySuperBoatHandler.hasSuperBoat(target.player))
        {
            throw ConditionFailedException("That player already has a superboat.")
        }

        val superBoat = EntitySuperBoat(
            target.player.location, size, target.player
        )

        EntitySuperBoatHandler.setupAndDisplaySuperBoat(target.player, superBoat)

        player.sendMessage("${CC.GREEN}Successfully created superboat.")
    }
}
