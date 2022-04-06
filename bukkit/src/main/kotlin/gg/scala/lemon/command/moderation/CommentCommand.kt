package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.CommentHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.result.CommentPaginatedResult
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 10/8/2021
 */
@CommandPermission("lemon.command.comment")
@CommandAlias("comment|note|comments|notes")
object CommentCommand : BaseCommand()
{

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("list")
    @Syntax("<player> [page]")
    @CommandCompletion("@all-players")
    @Description("List all comments attached to a player.")
    fun onList(sender: CommandSender, uniqueId: UUID, @Optional page: Int?)
    {
        sender.sendMessage("${CC.SEC}Loading comments for ${CC.PRI}${
            CubedCacheUtil.fetchName(uniqueId)
        }${CC.SEC}...")

        CommentHandler.fetchComments(target = uniqueId).thenAccept {
            CommentPaginatedResult.display(
                sender, listOf(*it.values.toTypedArray()),
                page ?: 1, command = "comment list ${CubedCacheUtil.fetchName(uniqueId)} %s"
            )
        }
    }

    @Subcommand("add|set")
    @Syntax("<player> <comment>")
    @CommandCompletion("@all-players")
    @Description("Attach a comment to a player.")
    fun onAdd(player: Player, uniqueId: UUID, comment: String)
    {
        CommentHandler.addCommentToPlayer(player, uniqueId, comment)
    }

    @Syntax("<id>")
    @Subcommand("remove|delete")
    @CommandCompletion("@all-players")
    @Description("Remove a comment from a player.")
    fun onRemove(player: Player, @Single id: String)
    {
        CommentHandler.removeCommentFromPlayer(player, id)
    }
}
