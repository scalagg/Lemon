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
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/9/2022
 */
@CommandAlias("profile|player")
@CommandPermission("op")
object ProfileCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

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
