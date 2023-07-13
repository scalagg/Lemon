package gg.scala.lemon.command.management

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.ne
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.result.RankPaginatedResult
import gg.scala.lemon.util.QuickAccess.replaceEmpty
import gg.scala.lemon.util.SplitUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AssignPermission
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.scope.ServerScope
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.Constants
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@AutoRegister
@CommandAlias("rank")
@CommandPermission("lemon.command.rank")
object RankCommand : ScalaCommand()
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

    @AssignPermission
    @Subcommand("scope add")
    @CommandCompletion("@ranks")
    fun onScopeAdd(
        sender: CommandSender, rank: Rank, @Single scope: String
    )
    {
        if (rank.scopes().any { it.group == scope.lowercase() })
        {
            throw ConditionFailedException(
                "The rank by the name ${CC.YELLOW}${rank.name}${CC.RED} already has a server scope with the group ${CC.YELLOW}$scope${CC.RED}."
            )
        }

        rank.scopes().add(ServerScope(scope.lowercase()))
        rank.saveAndPushUpdatesGlobally()

        sender.sendMessage(
            "${CC.SEC}The server scope with the group ${CC.PRI}${scope.lowercase()}${CC.SEC} has been added to the rank ${CC.PRI}${rank.getColoredName()}${CC.SEC}."
        )
    }

    @AssignPermission
    @Subcommand("scope remove")
    @CommandCompletion("@ranks @scopes")
    fun onScopeRemove(
        sender: CommandSender, rank: Rank, @Single scope: String
    )
    {
        if (!rank.scopes().any { it.group == scope.lowercase() })
        {
            throw ConditionFailedException(
                "The rank by the name ${CC.YELLOW}${rank.name}${CC.RED} does not have a server scope with the group ${CC.YELLOW}$scope${CC.RED}."
            )
        }

        rank.scopes().removeIf {
            it.group == scope.lowercase()
        }

        rank.saveAndPushUpdatesGlobally()

        sender.sendMessage(
            "${CC.SEC}The server scope with the group ${CC.PRI}${scope.lowercase()}${CC.SEC} has been removed from the rank ${CC.PRI}${rank.getColoredName()}${CC.SEC}."
        )
    }

    @AssignPermission
    @Subcommand("scope list")
    @CommandCompletion("@ranks")
    fun onScopeList(sender: CommandSender, rank: Rank)
    {
        if (rank.scopes().isEmpty())
        {
            throw ConditionFailedException(
                "The rank by the name ${CC.YELLOW}${rank.name}${CC.RED} is a global-scoped rank."
            )
        }

        sender.sendMessage("${CC.B_PRI}Scopes for rank ${CC.SEC}${rank.getColoredName()}${CC.B_PRI}:")

        for (scope in rank.scopes())
        {
            sender.sendMessage(
                " ${CC.WHITE}- ${scope.group}${
                    if (scope.individual.isEmpty()) "" else "${CC.GRAY} (${scope.individual.joinToString()})"
                }"
            )
        }
    }

    @AssignPermission
    @Subcommand("scope server list")
    @CommandCompletion("@ranks @scopes")
    fun onScopeServerList(sender: CommandSender, rank: Rank, @Single scope: String)
    {
        val serverScope = rank.scopes()
            .firstOrNull {
                it.group == scope.lowercase()
            }
            ?: throw ConditionFailedException(
                "The rank by the name ${CC.YELLOW}${rank.name}${CC.RED} does not have a server scope with the group ${CC.YELLOW}$scope${CC.RED}."
            )

        if (serverScope.individual.isEmpty())
        {
            throw ConditionFailedException(
                "The server scope with the group ${CC.YELLOW}$scope${CC.RED} does not have any server assignments."
            )
        }

        sender.sendMessage("${CC.B_PRI}Assigned servers for scope ${CC.SEC}$scope${CC.B_PRI}:")

        for (server in serverScope.individual)
        {
            sender.sendMessage(" ${CC.WHITE}- $server")
        }
    }

    @AssignPermission
    @Subcommand("scope server add")
    @CommandCompletion("@ranks @scopes")
    fun onScopeServerAdd(
        sender: CommandSender, rank: Rank, @Single scope: String, @Single server: String
    )
    {
        val serverScope = rank.scopes()
            .firstOrNull {
                it.group == scope.lowercase()
            }
            ?: throw ConditionFailedException(
                "The rank by the name ${CC.YELLOW}${rank.name}${CC.RED} does not have a server scope with the group ${CC.YELLOW}$scope${CC.RED}."
            )

        if (serverScope.individual.contains(server.lowercase()))
        {
            throw ConditionFailedException(
                "The server scope with the group ${CC.YELLOW}$scope${CC.RED} already contains the server assignment ${CC.YELLOW}$server${CC.RED}."
            )
        }

        serverScope.individual.add(server)
        rank.saveAndPushUpdatesGlobally()

        sender.sendMessage(
            "${CC.SEC}The server assignment with the ID ${CC.PRI}$server${CC.SEC} has been added to the server scope ${CC.PRI}$server${CC.SEC}."
        )
    }

    @AssignPermission
    @Subcommand("scope server remove")
    @CommandCompletion("@ranks @scopes @scopes:servers")
    fun onScopeServerRemove(
        sender: CommandSender, rank: Rank, @Single scope: String, @Single server: String
    )
    {
        val serverScope = rank.scopes()
            .firstOrNull {
                it.group == scope.lowercase()
            }
            ?: throw ConditionFailedException(
                "The rank by the name ${CC.YELLOW}${rank.name}${CC.RED} does not have a server scope with the group ${CC.YELLOW}$scope${CC.RED}."
            )

        if (!serverScope.individual.contains(server.lowercase()))
        {
            throw ConditionFailedException(
                "The server scope with the group ${CC.YELLOW}$scope${CC.RED} does not contains the server assignment ${CC.YELLOW}$server${CC.RED}."
            )
        }

        serverScope.individual
            .removeIf {
                it == server.lowercase()
            }
        rank.saveAndPushUpdatesGlobally()

        sender.sendMessage(
            "${CC.SEC}The scope with the group ${CC.PRI}$scope${CC.SEC} has the server assignment ${CC.PRI}$server${CC.SEC} removed from it."
        )
    }

    @AssignPermission
    @CommandCompletion("@ranks")
    @Subcommand("view")
    @Description("View information for a certain rank.")
    fun onInfo(sender: CommandSender, rank: Rank)
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

        if (rank.scopes().isNotEmpty())
        {
            sender.sendMessage("${CC.GRAY}Scopes:")

            for (scope in rank.scopes())
            {
                sender.sendMessage(
                    " ${CC.WHITE}- ${scope.group}${
                        if (scope.individual.isEmpty()) "" else "${CC.GRAY} (${scope.individual.joinToString()})"
                    }"
                )
            }

            sender.sendMessage("")
        }

        sender.sendMessage("${CC.GRAY}Children: ${if (rank.children.isEmpty()) "${CC.RED}None" else ""}")

        if (rank.children.isNotEmpty())
        {
            rank.children.forEach {
                val child = RankHandler.findRank(it)

                if (child != null)
                {
                    sender.sendMessage("${CC.GRAY} - ${CC.WHITE}${child.getColoredName()}")
                }
            }

            sender.sendMessage("")
        }

        sender.sendMessage("${CC.GRAY}Permissions: ${if (rank.permissions.isEmpty()) "${CC.RED}None" else ""}")

        if (rank.permissions.isNotEmpty())
        {
            val bungee = rank.permissions.filter { it.startsWith("%") }
            val spigot = rank.permissions.filter { !it.startsWith("%") }

            if (bungee.isNotEmpty())
            {
                sender.sendMessage(" ${CC.GRAY}Proxy Level:")

                bungee.forEach {
                    sender.sendMessage(
                        "${CC.GRAY}  - ${CC.WHITE}${
                            it.removePrefix("%").removePrefix("-")
                        } ${CC.GRAY}"
                    )
                }
            }

            if (spigot.isNotEmpty())
            {
                sender.sendMessage(" ${CC.GRAY}Spigot Level:")

                spigot.forEach {
                    sender.sendMessage(
                        "${CC.GRAY}  - ${CC.WHITE}${
                            it.removePrefix("%").removePrefix("-")
                        } ${CC.GRAY}"
                    )
                }
            }
        }
    }

    @AssignPermission
    @Subcommand("create")
    @Description("Create a new rank.")
    fun onCreate(sender: CommandSender, @Single name: String)
    {
        val existing = RankHandler.findRank(name)

        if (existing != null)
        {
            throw ConditionFailedException("A rank with the name matching ${CC.YELLOW}$name${CC.RED} already exists.")
        }

        val rank = Rank(UUID.randomUUID(), name)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've created the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }

    @AssignPermission
    @Subcommand("clone")
    @CommandCompletion("@ranks")
    @Description("Create a new rank by cloning an existing one.")
    fun onClone(sender: CommandSender, rank: Rank, @Single name: String)
    {
        val existing = RankHandler.findRank(name)

        if (existing != null)
        {
            throw ConditionFailedException(
                "A rank with the name matching ${CC.YELLOW}$name${CC.RED} already exists."
            )
        }

        val newRank = rank
            .copy(
                uuid = UUID.randomUUID(),
                name = name
            )

        newRank
            .saveAndPushUpdatesGlobally()
            .thenAccept {
                sender.sendMessage(
                    "${CC.SEC}You've created the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank by cloning it from the existing ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank."
                )
            }
    }

    @AssignPermission
    @Subcommand("delete")
    @CommandCompletion("@ranks")
    @Description("Delete an existing rank.")
    fun onDelete(sender: CommandSender, rank: Rank)
    {
        if (rank.uuid == RankHandler.getDefaultRank().uuid)
        {
            throw ConditionFailedException("You're not allowed to delete the ${CC.YELLOW}${rank.name}${CC.RED} rank.")
        }

        DataStoreObjectControllerCache.findNotNull<Rank>()
            .delete(rank.uuid, DataStoreStorageType.MONGO)
            .thenAccept {
                RedisHandler.buildMessage(
                    "rank-delete",
                    hashMapOf<String, String>().also {
                        it["uniqueId"] = rank.uuid.toString()
                    }
                ).publish()

                sender.sendMessage("${CC.SEC}You've deleted the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
            }
    }

    @AssignPermission
    @Subcommand("meta prefix")
    @CommandCompletion("@ranks")
    @Description("Edit a rank's prefix.")
    fun onMetaPrefix(sender: CommandSender, rank: Rank, prefix: String)
    {
        rank.prefix = Color.translate(prefix)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} prefix to ${CC.WHITE}${rank.prefix}${CC.SEC}.")
        }
    }

    @AssignPermission
    @Subcommand("meta display")
    @CommandCompletion("@ranks")
    @Description("Edit a rank's display name.")
    fun onMetaDisplay(sender: CommandSender, rank: Rank, display: String)
    {
        rank.displayName = Color.translate(display)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} display name to ${CC.WHITE}${rank.prefix}${CC.SEC}.")
        }
    }

    @AssignPermission
    @Subcommand("meta name")
    @CommandCompletion("@ranks")
    @Description("Edit a rank's name.")
    fun onMetaName(sender: CommandSender, rank: Rank, name: String)
    {
        rank.name = ChatColor.stripColor(
            Color.translate(name)
        )

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} name to ${CC.WHITE}${rank.name}${CC.SEC}.")
        }
    }

    @AssignPermission
    @Subcommand("meta suffix")
    @CommandCompletion("@ranks")
    @Description("Edit a rank's suffix.")
    fun onMetaSuffix(sender: CommandSender, rank: Rank, suffix: String)
    {
        rank.suffix = Color.translate(suffix)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} suffix to ${CC.WHITE}${rank.suffix}${CC.SEC}.")
        }
    }

    @AssignPermission
    @Subcommand("meta color")
    @CommandCompletion("@ranks")
    @Description("Edit a rank's color.")
    fun onMetaColor(sender: CommandSender, rank: Rank, color: String)
    {
        rank.color = Color.translate(color)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} color to ${CC.WHITE}${rank.color}this${CC.SEC}.")
        }
    }

    @AssignPermission
    @Subcommand("meta visible")
    @CommandCompletion("@ranks")
    @Description("Edit a rank's visibility.")
    fun onMetaVisible(sender: CommandSender, rank: Rank, visibility: Boolean)
    {
        rank.visible = visibility

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} visibility to ${CC.WHITE}${rank.visible}${CC.SEC}.")
        }
    }

    @AssignPermission
    @Subcommand("meta grantable")
    @CommandCompletion("@ranks")
    @Description("Edit a rank's grantability.")
    fun onMetaGrantable(sender: CommandSender, rank: Rank, grantability: Boolean)
    {
        rank.grantable = grantability

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} grantability to ${CC.WHITE}${rank.grantable}${CC.SEC}.")
        }
    }

    @AssignPermission
    @Subcommand("meta weight")
    @CommandCompletion("@ranks")
    @Description("Edit a rank's weight.")
    fun onMetaWeight(sender: CommandSender, rank: Rank, weight: Int)
    {
        rank.weight = weight

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} weight to ${CC.WHITE}${rank.weight}${CC.SEC}.")
        }
    }

    @AssignPermission
    @Subcommand("child tree")
    @CommandCompletion("@ranks")
    @Description("View all rank children in tree form.")
    fun onChildTree(sender: CommandSender, rank: Rank)
    {
        if (rank.children.isEmpty())
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} has no children.")
        }

        sender.sendMessage(
            arrayOf(
                "${CC.PRI}${CC.STRIKE_THROUGH}-----------------------------------",
                "${CC.WHITE}Children ranks of ${rank.getColoredName()}${CC.WHITE}:",
            )
        )

        fun recursiveChildSearch(
            childRank: Rank, node: Int = 1
        )
        {
            if (childRank.children.isEmpty())
            {
                return
            }

            childRank.children
                .mapNotNull { RankHandler.findRank(it) }
                .sortedBy { it.children.size }
                .forEach { nodeRank ->
                    sender.sendMessage(
                        "${"  ".repeat(node)}${CC.GRAY}${
                            Constants.THIN_VERTICAL_LINE
                        } ${
                            nodeRank.getColoredName()
                        }"
                    )

                    recursiveChildSearch(
                        nodeRank, node = node + 1
                    )
                }
        }

        recursiveChildSearch(rank)
        sender.sendMessage("${CC.PRI}${CC.STRIKE_THROUGH}-----------------------------------")
    }

    @AssignPermission
    @Subcommand("child add")
    @CommandCompletion("@ranks @ranks")
    @Description("Add a child to a rank.")
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

    @AssignPermission
    @Subcommand("child remove")
    @CommandCompletion("@ranks @ranks")
    @Description("Remove a child from a rank.")
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

    @AssignPermission
    @Subcommand("permission list")
    @CommandCompletion("@ranks")
    @Description("View all permissions for a rank.")
    fun onPermissionList(sender: CommandSender, rank: Rank)
    {
        if (rank.permissions.isEmpty())
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} has no permissions.")
        }

        sender.sendMessage(
            arrayOf(
                "${CC.PRI}${CC.STRIKE_THROUGH}-----------------------------------",
                "${CC.WHITE}Permission nodes of ${rank.getColoredName()}${CC.WHITE}:",
                "Spigot nodes:"
            )
        )

        rank.permissions
            .filter { !it.startsWith("%") }
            .forEach {
                sender.sendMessage(" ${CC.GRAY}${Constants.THIN_VERTICAL_LINE}${CC.WHITE} $it")
            }

        sender.sendMessage(
            "Proxy nodes:",
        )

        rank.permissions
            .filter { it.startsWith("%") }
            .forEach {
                sender.sendMessage(" ${CC.GRAY}${Constants.THIN_VERTICAL_LINE}${CC.WHITE} ${it.removePrefix("%")}")
            }

        sender.sendMessage(
            "Blacklisted nodes:",
        )

        rank.permissions
            .filter { it.startsWith("*") }
            .forEach {
                sender.sendMessage(" ${CC.GRAY}${Constants.THIN_VERTICAL_LINE}${CC.WHITE} ${it.removePrefix("*")}")
            }

        sender.sendMessage(
            "${CC.PRI}${CC.STRIKE_THROUGH}-----------------------------------"
        )
    }

    @AssignPermission
    @Subcommand("permission add")
    @CommandCompletion("@ranks")
    @Description("Add a permission to a rank.")
    fun onPermissionAdd(sender: CommandSender, rank: Rank, permission: String)
    {
        if (rank.permissions.contains(permission))
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} already has the ${CC.YELLOW}${permission}${CC.RED} permission.")
        }

        rank.permissions.add(permission)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage(
                "${CC.SEC}You've added the${
                    if (permission.startsWith("%"))
                    {
                        " proxy-level"
                    } else if (permission.startsWith("*"))
                    {
                        " blacklisted"
                    } else
                    {
                        ""
                    }
                } permission ${CC.WHITE}${
                    permission.replace("%", "").replace("*", "")
                }${CC.SEC} to the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank."
            )
        }
    }

    @AssignPermission
    @Subcommand("permission remove")
    @CommandCompletion("@ranks @permissions")
    @Description("Remove a permission from a rank.")
    fun onPermissionRemove(sender: CommandSender, rank: Rank, permission: String)
    {
        if (!rank.permissions.contains(permission))
        {
            throw ConditionFailedException("${CC.YELLOW}${rank.name}${CC.RED} doesn't have the permission node ${CC.YELLOW}${permission}${CC.RED}.")
        }

        rank.permissions.remove(permission)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've remove the permission ${CC.WHITE}$permission${CC.SEC} from the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }

    @AssignPermission
    @Subcommand("tools inherit-from-start")
    @Description("Make all ranks inherit the rank before them.")
    fun onToolsInheritStart(sender: CommandSender)
    {
        val sorted = RankHandler.sortedI

        for (withIndex in sorted.withIndex())
        {
            if (withIndex.index - 1 < 0)
            {
                continue
            }

            try
            {
                val rankBefore = sorted[withIndex.index - 1]

                if (rankBefore.uuid == withIndex.value.uuid)
                {
                    continue
                }

                if (!withIndex.value.children.contains(rankBefore.uuid))
                {
                    withIndex.value.children.add(rankBefore.uuid)
                    withIndex.value.saveAndPushUpdatesGlobally()

                    sender.sendMessage("${CC.GREEN}Added ${rankBefore.getColoredName()}${CC.GREEN} as a child of ${withIndex.value.getColoredName()}${CC.GREEN}.")
                }
            } catch (ignored: Exception)
            {
            }
        }
    }

    @AssignPermission
    @Subcommand("tools inherit")
    @Description("Make all ranks inherit a specified rank.")
    fun onToolsInherit(sender: CommandSender, rank: Rank)
    {
        val ranksToModify = RankHandler.ranks.filter {
            !it.value.children.contains(rank.uuid)
        }

        if (ranksToModify.isEmpty())
        {
            throw ConditionFailedException("All ranks already inherit the ${CC.YELLOW}${rank.name}${CC.RED} rank.")
        }

        ranksToModify.forEach {
            if (it.value.uuid == rank.uuid)
            {
                return@forEach
            }

            it.value.children.add(rank.uuid)
            it.value.saveAndPushUpdatesGlobally()
        }

        sender.sendMessage("${CC.SEC}Modified ${CC.PRI}${ranksToModify.size}${CC.SEC} ranks.")
    }

    @AssignPermission
    @Subcommand("tools clear-all-inheritances")
    @Description("Clear all inheritances for all ranks.")
    fun onToolsClear(sender: CommandSender)
    {
        val ranksToModify = RankHandler.ranks.filter {
            it.value.children.isNotEmpty()
        }

        if (ranksToModify.isEmpty())
        {
            throw ConditionFailedException("There are no ranks to modify.")
        }

        ranksToModify.forEach {
            it.value.children.clear()
            it.value.saveAndPushUpdatesGlobally()
        }

        sender.sendMessage("${CC.SEC}Modified ${CC.PRI}${ranksToModify.size}${CC.SEC} ranks.")
    }

    @AssignPermission
    @Subcommand("tools clear-all-permissions")
    @Description("Clear all permissions for all ranks.")
    fun onToolsClearPermissions(player: Player)
    {
        RankHandler.ranks.forEach {
            it.value.permissions.clear()
            it.value.saveAndPushUpdatesGlobally()

            player.sendMessage("${CC.GREEN}Cleared ${it.value.getColoredName()}'s ${CC.GREEN}permissions.")
        }
    }

    @AssignPermission
    @CommandCompletion("@ranks")
    @Subcommand("scope clear")
    @Description("Clear all scopes for a ranks.")
    fun onToolsClearPermissions(player: Player, rank: Rank)
    {
        rank.scopes().clear()
        rank.saveAndPushUpdatesGlobally()

        player.sendMessage(
            "${CC.SEC}Cleared scopes for rank ${CC.PRI}${rank.name}${CC.SEC}!"
        )
    }

    @AssignPermission
    @CommandCompletion("@ranks")
    @Subcommand("meta listrank")
    @Description("List all players applied to a rank.")
    fun onMetaListRank(player: Player, rank: Rank): CompletableFuture<Void>
    {
        if (rank.uuid == RankHandler.getDefaultRank().uuid)
        {
            throw ConditionFailedException("You may not do a listrank search on the default rank.")
        }

        player.sendMessage("${CC.SEC}Fetching...")

        return DataStoreObjectControllerCache
            .findNotNull<Grant>()
            .mongo()
            .loadAllWithFilter(
                Filters.and(
                    eq("rankId", rank.uuid.toString()),
                    ne("removedAt", rank.uuid.toString())
                )
            )
            .thenAccept {
                val rankScoped = it.values
                    .filter { grant ->
                        grant.rankId == rank.uuid && grant.isActive
                    }

                val users = rankScoped
                    .map(Grant::target)
                    .toSet()
                    .map(CubedCacheUtil::fetchName)

                player.sendMessage(
                    "${CC.SEC}Accounts with the ${rank.getColoredName()}${CC.SEC} rank ${CC.GRAY}(${users.size})${CC.SEC}:"
                )

                player.sendMessage(
                    users.take(50).joinToString(", ")
                )

                if (users.size > 50)
                {
                    player.sendMessage("${CC.RED}Showing first 25 users)")
                }
            }
    }

    @AssignPermission
    @Subcommand("tools clear-duplicates")
    @Description("Clear all duplicate permissions for all ranks.")
    fun onToolsClearDuplicates(player: Player)
    {
        RankHandler.sorted.forEach { rank ->
            var removed = 0

            rank.permissions.toList().forEach { permission ->
                var shouldRemove = false

                rank.children.forEach { child ->
                    val childRank = RankHandler.findRank(child)

                    if (childRank != null)
                    {
                        if (childRank.getCompoundedPermissions().contains(permission))
                        {
                            shouldRemove = true
                        }
                    }
                }

                if (shouldRemove)
                {
                    rank.permissions.remove(permission)
                    removed++
                }
            }

            player.sendMessage("${CC.GREEN}Removed ${CC.D_AQUA}$removed${CC.GREEN} duplicate permissions from ${rank.getColoredName()}${CC.GREEN}.")
        }
    }
}
