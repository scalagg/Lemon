package com.solexgames.lemon.command.management

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.RedisHandler
import com.solexgames.lemon.player.rank.Rank
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import org.apache.commons.lang3.StringUtils
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@CommandAlias("rank")
@CommandPermission("lemon.command.rank")
class RankCommand : BaseCommand() {

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("create")
    @Description("Create a new rank.")
    @CommandPermission("lemon.command.rank.management")
    fun onCreate(sender: CommandSender, name: String) {
        val existing = Lemon.instance.rankHandler.findRank(name)

        if (existing != null) {
            throw ConditionFailedException("A rank with the name matching ${CC.YELLOW}$name${CC.RED} already exists.")
        }

        if (name.length < 3) {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must be at least 3 characters long.")
        }

        if (name.length > 16) {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must be at most 16 characters long.")
        }

        if (!StringUtils.isAlphanumeric(name)) {
            throw ConditionFailedException("${CC.YELLOW}$name${CC.RED} must only contain alphanumeric characters.")
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
    fun onDelete(sender: CommandSender, rank: Rank) {
        if (rank.uuid == Lemon.instance.rankHandler.getDefaultRank().uuid) {
            throw ConditionFailedException("You're not allowed to delete the ${CC.YELLOW}${rank.name}${CC.RED} rank.")
        }

        Lemon.instance.mongoHandler.rankLayer.deleteEntry(rank.uuid.toString()).thenAccept {
            RedisHandler.buildMessage(
                "rank-delete",
                hashMapOf<String, String>().also {
                    it["uniqueId"] = rank.uuid.toString()
                }
            )

            Lemon.instance.rankHandler.ranks.remove(rank.uuid)

            sender.sendMessage("${CC.SEC}You've delete the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
        }
    }

    @Subcommand("meta prefix")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks prefix.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaPrefix(sender: CommandSender, rank: Rank, prefix: String) {
        rank.prefix = Color.translate(prefix)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} prefix to ${CC.WHITE}${rank.prefix}${CC.SEC}.")
        }
    }

    @Subcommand("meta suffix")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks suffix.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaSuffix(sender: CommandSender, rank: Rank, suffix: String) {
        rank.suffix = Color.translate(suffix)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} suffix to ${CC.WHITE}${rank.suffix}${CC.SEC}.")
        }
    }

    @Subcommand("meta color")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks color.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaColor(sender: CommandSender, rank: Rank, color: String) {
        rank.color = Color.translate(color)

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} color to ${CC.WHITE}${rank.color}this${CC.SEC}.")
        }
    }

    @Subcommand("meta visible")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks visibility.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaVisible(sender: CommandSender, rank: Rank, visibility: Boolean) {
        rank.visible = visibility

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} visibility to ${CC.WHITE}${rank.visible}${CC.SEC}.")
        }
    }

    @Subcommand("meta weight")
    @CommandCompletion("@ranks")
    @Description("Edit a ranks weight.")
    @CommandPermission("lemon.command.rank.meta.edit")
    fun onMetaWeight(sender: CommandSender, rank: Rank, weight: Int) {
        rank.weight = weight

        rank.saveAndPushUpdatesGlobally().thenAccept {
            sender.sendMessage("${CC.SEC}You've updated ${CC.PRI}${rank.getColoredName()}'s${CC.SEC} weight to ${CC.WHITE}${rank.weight}${CC.SEC}.")
        }
    }
}
