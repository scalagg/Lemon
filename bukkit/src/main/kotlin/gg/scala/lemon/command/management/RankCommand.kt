package gg.scala.lemon.command.management

import gg.scala.lemon.handler.DataStoreHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.result.RankPaginatedResult
import gg.scala.lemon.util.QuickAccess.replaceEmpty
import gg.scala.lemon.util.SplitUtil
import gg.scala.lemon.util.dispatchImmediately
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@CommandAlias("rank")
@CommandPermission("lemon.command.rank")
class RankCommand : BaseCommand()
{

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Syntax("[page]")
    @Subcommand("list")
    @Description("View all ranks.")
    fun onList(sender: CommandSender, @Optional page: Int?)
    {
        val rankList = RankHandler.sorted

        if (rankList.isEmpty())
        {
            throw ConditionFailedException("There are no ranks.")
        }

        RankPaginatedResult.display(
            sender, rankList, page ?: 1, "rank list %s"
        )
    }

    @CommandCompletion("@ranks")
    @Subcommand("view|info|information")
    @Description("View information for a certain rank.")
    fun onList(sender: CommandSender, rank: Rank)
    {
        sender.sendMessage("${CC.B_PRI}${rank.name} Information:")
        sender.sendMessage("")
        sender.sendMessage("${CC.GRAY}Name: ${CC.WHITE}${rank.getColoredName()}")
        sender.sendMessage("${CC.GRAY}ID: ${CC.WHITE}${SplitUtil.splitUuid(rank.uuid)}")
        sender.sendMessage("${CC.GRAY}Weight: ${CC.WHITE}${rank.weight}")
        sender.sendMessage("")
        sender.sendMessage("${CC.GRAY}Prefix: ${CC.WHITE}${replaceEmpty(rank.prefix)}")
        sender.sendMessage("${CC.GRAY}Suffix: ${CC.WHITE}${replaceEmpty(rank.suffix)}")
        sender.sendMessage("${CC.GRAY}Color: ${CC.WHITE}${rank.color}this")
        sender.sendMessage("")
        sender.sendMessage("${CC.GRAY}Visible: ${CC.WHITE}${rank.visible}")
        sender.sendMessage("")

        sender.sendMessage("${CC.GRAY}Children: ${if (rank.children.isEmpty()) "${CC.RED}None" else ""}")

        if (rank.children.isNotEmpty())
        {
            rank.children.forEach {
                val child = RankHandler.findRank(it)

                if (child != null)
                {
                    sender.sendMessage("${CC.GRAY} - ${CC.WHITE}${rank.getColoredName()}")
                }
            }

            sender.sendMessage("")
        }

        sender.sendMessage("${CC.GRAY}Permissions: ${if (rank.permissions.isEmpty()) "${CC.RED}None" else ""}")

        if (rank.permissions.isNotEmpty())
        {
            rank.permissions.forEach {
                sender.sendMessage("${CC.GRAY} - ${CC.WHITE}$it")
            }
        }
    }

    @Subcommand("create")
    @Description("Create a new rank.")
    @CommandPermission("lemon.command.rank.management")
    fun onCreate(sender: CommandSender, name: String)
    {
        val existing = RankHandler.findRank(name)

        if (existing != null)
        {
            throw ConditionFailedException("A rank with the name matching ${CC.YELLOW}$name${CC.RED} already exists.")
        }

        if (name.length < 3)
        {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must be at least 3 characters long.")
        }

        if (name.length > 16)
        {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must be at most 16 characters long.")
        }

        val rank = Rank(name)
        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've created the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }

    @Subcommand("delete")
    @CommandCompletion("@ranks")
    @Description("Delete an existing rank.")
    @CommandPermission("lemon.command.rank.management")
    fun onDelete(sender: CommandSender, rank: Rank)
    {
        if (rank.uuid == RankHandler.getDefaultRank().uuid)
        {
            throw ConditionFailedException("You're not allowed to delete the ${CC.YELLOW}${rank.name}${CC.RED} rank.")
        }

        DataStoreHandler.rankLayer.deleteEntry(rank.uuid.toString()).thenAccept {
            RedisHandler.buildMessage(
                "rank-delete",
                hashMapOf<String, String>().also {
                    it["uniqueId"] = rank.uuid.toString()
                }
            ).dispatchImmediately()

            sender.sendMessage("${CC.SEC}You've deleted the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }

    @Subcommand("meta prefix")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks prefix.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaPrefix(sender: CommandSender, rank: Rank, prefix: String)
    {
        rank.prefix = Color.translate(prefix)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} prefix to ${CC.WHITE}${rank.prefix}${CC.SEC}.")
        }
    }

    @Subcommand("meta suffix")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks suffix.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaSuffix(sender: CommandSender, rank: Rank, suffix: String)
    {
        rank.suffix = Color.translate(suffix)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} suffix to ${CC.WHITE}${rank.suffix}${CC.SEC}.")
        }
    }

    @Subcommand("meta color")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks color.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaColor(sender: CommandSender, rank: Rank, color: String)
    {
        rank.color = Color.translate(color)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} color to ${CC.WHITE}${rank.color}this${CC.SEC}.")
        }
    }

    @Subcommand("meta visible")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks visibility.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaVisible(sender: CommandSender, rank: Rank, visibility: Boolean)
    {
        rank.visible = visibility

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} visibility to ${CC.WHITE}${rank.visible}${CC.SEC}.")
        }
    }

    @Subcommand("meta weight")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks weight.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaWeight(sender: CommandSender, rank: Rank, weight: Int)
    {
        rank.weight = weight

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} weight to ${CC.WHITE}${rank.weight}${CC.SEC}.")
        }
    }

    @Subcommand("child list")
    @CommandCompletion("@ranks")
    @Description("View all available children.")
    @CommandPermission("lemon.command.rank.child.edit")
    fun onChildList(sender: CommandSender, rank: Rank)
    {
        if (rank.children.isEmpty())
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} has no children.")
        }

        sender.sendMessage(
            arrayOf(
                "${CC.B_PRI}${rank.name}'s Children:",
                "${CC.SEC}${rank.children.size}${CC.GRAY} children found.",
                ""
            )
        )

        rank.children.forEach {
            val child = RankHandler.findRank(it)

            if (child != null)
            {
                sender.sendMessage("${CC.GRAY} - ${CC.WHITE}${rank.getColoredName()}")
            }
        }
    }

    @Subcommand("child add")
    @CommandCompletion("@ranks @ranks")
    @Description("Add a child to a rank.")
    @CommandPermission("lemon.command.rank.child.edit")
    fun onChildAdd(sender: CommandSender, rank: Rank, child: Rank)
    {
        if (rank.children.contains(child.uuid))
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} already has the child rank ${CC.YELLOW}${child.name}${CC.RED}.")
        }

        rank.children.add(child.uuid)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've added the child rank ${CC.PRI}${child.getColoredName()}${CC.SEC} to the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }

    @Subcommand("child remove")
    @CommandCompletion("@ranks @ranks")
    @Description("Remove a child from a rank.")
    @CommandPermission("lemon.command.rank.child.edit")
    fun onChildRemove(sender: CommandSender, rank: Rank, child: Rank)
    {
        if (!rank.children.contains(child.uuid))
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} does not inherit the child rank ${CC.YELLOW}${child.name}${CC.RED}.")
        }

        rank.children.remove(child.uuid)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've removed the child rank ${CC.PRI}${child.getColoredName()}${CC.SEC} from the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }

    @Subcommand("permission list")
    @CommandCompletion("@ranks")
    @Description("View all permissions for a rank.")
    @CommandPermission("lemon.command.rank.permission.edit")
    fun onPermissionList(sender: CommandSender, rank: Rank)
    {
        if (rank.permissions.isEmpty())
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} has no permissions.")
        }

        sender.sendMessage(
            arrayOf(
                "${CC.B_PRI}${rank.name}'s Permissions:",
                "${CC.SEC}${rank.permissions.size}${CC.GRAY} permissions found.",
                ""
            )
        )

        rank.permissions.forEach {
            sender.sendMessage("${CC.GRAY} - ${CC.WHITE}$it")
        }
    }

    @Subcommand("permission add")
    @CommandCompletion("@ranks")
    @Description("Add a permission to a rank.")
    @CommandPermission("lemon.command.rank.permission.edit")
    fun onPermissionAdd(sender: CommandSender, rank: Rank, permission: String)
    {
        if (rank.permissions.contains(permission))
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} already has the ${CC.YELLOW}${permission}${CC.RED} permission.")
        }

        rank.permissions.add(permission)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've added the permission ${CC.WHITE}$permission${CC.SEC} to the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }

    @Subcommand("permission remove")
    @CommandCompletion("@ranks")
    @Description("Remove a permission from a rank.")
    @CommandPermission("lemon.command.rank.permission.edit")
    fun onPermissionRemove(sender: CommandSender, rank: Rank, permission: String)
    {
        if (!rank.permissions.contains(permission))
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} does not have the ${CC.YELLOW}${permission}${CC.RED} permission.")
        }

        rank.permissions.remove(permission)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've remove the permission ${CC.WHITE}$permission${CC.SEC} from the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }
}
