package gg.scala.lemon.command.management

import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AssignPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/9/2022
 */
@AutoRegister
@CommandAlias("user|users|u")
@CommandPermission("lemon.command.user")
object UserCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @AssignPermission
    @Subcommand("disguise")
    @CommandCompletion("@ranks @players")
    @Description("Set a player's display rank.")
    fun onDisguiseRank(sender: CommandSender, target: LemonPlayer, rank: Rank)
    {
        target.disguiseRankUniqueId = rank.uuid
        QuickAccess.reloadPlayer(target.uniqueId, false)

        sender.sendMessage("${CC.GREEN}You've set ${target.getColoredName(ignoreMinequest = true)}'s${CC.GREEN} disguise rank to ${rank.getColoredName()}${CC.GREEN}.")
    }

    @AssignPermission
    @Subcommand("undisguise")
    @CommandCompletion("@players")
    @Description("Set a player's display rank to their original one.")
    fun onUnDisguiseRank(sender: CommandSender, target: LemonPlayer)
    {
        target.disguiseRankUniqueId = null
        QuickAccess.reloadPlayer(target.uniqueId, false)

        sender.sendMessage("${CC.WHITE}${target.name}${CC.GREEN} has been undisguised.")
    }

    @AssignPermission
    @Subcommand("disguise all")
    @CommandCompletion("@ranks")
    @Description("Set all online players' display ranks.")
    fun onDisguiseRankAll(sender: CommandSender, rank: Rank)
    {
        for (value in PlayerHandler.players.values)
        {
            value.disguiseRankUniqueId = rank.uuid
            QuickAccess.reloadPlayer(value.uniqueId, false)
        }

        sender.sendMessage("${CC.GREEN}You've set everyone's disguise rank to ${rank.getColoredName()}${CC.GREEN}.")
    }

    @AssignPermission
    @Subcommand("undisguise all")
    @Description("Set all online players' display ranks to their original ones.")
    fun onUnDisguiseRankAll(sender: CommandSender)
    {
        for (value in PlayerHandler.players.values)
        {
            value.disguiseRankUniqueId = null
            QuickAccess.reloadPlayer(value.uniqueId, false)
        }

        sender.sendMessage("${CC.GREEN}All online players are now shown as their original rank.")
    }

    @AssignPermission
    @Subcommand("permissions view")
    @CommandCompletion("@players")
    @Description("View all persistent player-specific permissions for a player.")
    fun onViewPermissions(
        sender: CommandSender,
        target: AsyncLemonPlayer
    ): CompletableFuture<Void>
    {
        return target.validatePlayers(
            sender, false
        ) {
            sender.sendMessage("${CC.B_GOLD}${ChatColor.UNDERLINE}Player permissions for ${it.name}:")

            it.assignedPermissions
                .sortedByDescending { permission -> permission }
                .forEach { permission ->
                    sender.sendMessage(" - $permission")
                }
        }
    }

    @AssignPermission
    @Subcommand("permissions add")
    @CommandCompletion("@players")
    @Description("Assign a persistent player-specific permission.")
    fun onAssignPermission(
        sender: CommandSender,
        target: AsyncLemonPlayer,
        node: String
    ): CompletableFuture<Void>
    {
        return target.validatePlayers(
            sender, false
        ) {
            if (node.lowercase() in it.assignedPermissions)
            {
                throw ConditionFailedException(
                    "The permission node ${CC.WHITE}$node${CC.RED} has already been assigned to ${CC.YELLOW}${it.name}${CC.RED}."
                )
            }

            it.assignedPermissions += node.lowercase()
            it.save().join()

            sender.sendMessage("${CC.SEC}You have assigned the permission node ${CC.WHITE}$node${CC.SEC} to ${CC.GREEN}${it.name}'s${CC.SEC} profile.")
        }
    }

    @AssignPermission
    @CommandCompletion("@players")
    @Subcommand("permissions remove")
    @Description("Remove a player-specific permission.")
    fun onRemovePermission(
        sender: CommandSender,
        target: AsyncLemonPlayer,
        node: String
    ): CompletableFuture<Void>
    {
        return target.validatePlayers(
            sender, false
        ) {
            if (node.lowercase() !in it.assignedPermissions)
            {
                throw ConditionFailedException(
                    "The permission node ${CC.WHITE}$node${CC.RED} has not been assigned to ${CC.YELLOW}${it.name}${CC.RED}."
                )
            }

            it.assignedPermissions -= node.lowercase()
            it.save().join()

            sender.sendMessage("${CC.SEC}You have removed the permission node ${CC.WHITE}$node${CC.SEC} from ${CC.GREEN}${it.name}'s${CC.SEC} profile.")
        }
    }
}
