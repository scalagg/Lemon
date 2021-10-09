package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.CommentHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.comment.paginated.CommentPaginatedResult
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/8/2021
 */
@CommandPermission("lemon.command.comment")
@CommandAlias("comment|note|comments|notes")
class CommentCommand : BaseCommand()
{

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("list")
    @Syntax("<player> [page]")
    @Description("List all comments attached to a player.")
    fun onList(sender: CommandSender, target: LemonPlayer, @Optional page: Int?)
    {
        sender.sendMessage("${CC.GRAY}Loading comments for ${target.getColoredName()}${CC.GRAY}...")

        CommentHandler.fetchComments(target = target.uniqueId).thenAccept {
            CommentPaginatedResult.display(
                sender, listOf(*it.values.toTypedArray()),
                page ?: 1, command = "comment list %s"
            )
        }
    }

    @Subcommand("add|set")
    @Syntax("<player> <comment>")
    @Description("Attach a comment to a player.")
    fun onAdd(player: Player, target: LemonPlayer, comment: String)
    {
        CommentHandler.addCommentToPlayer(player, target.uniqueId, comment)
    }

    @Syntax("<id>")
    @Subcommand("remove|delete")
    @Description("Remove a comment from a player.")
    fun onRemove(player: Player, @Single id: String)
    {
        CommentHandler.removeCommentFromPlayer(player, id)
    }
}
