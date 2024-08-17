package gg.scala.lemon.command.management

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.lemon.filter.auditing.MessageAuditLog
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.store.controller.DataStoreObjectControllerCache
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.time.TimeUtil
import net.md_5.bungee.api.chat.ClickEvent
import java.time.Duration
import java.util.*

/**
 * @author GrowlyX
 * @since 8/16/2024
 */
@AutoRegister
object ChatHistoryCommand : ScalaCommand()
{
    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("chathistory|ch")
    @CommandPermission("lemon.command.chathistory")
    fun chatHistory(
        player: ScalaPlayer,
        target: AsyncLemonPlayer,
        @Optional page: Int?
    ) = target.validatePlayers(player.bukkit(), false) {
        if ((page ?: 1) < 1)
        {
            throw ConditionFailedException(
                "This player has no chat history on Page #${page ?: 1}!"
            )
        }

        val chatHistory = preLoadChatHistory(it.uniqueId, pageNumber = page ?: 1)
        if (chatHistory.isEmpty())
        {
            throw ConditionFailedException(
                "This player has no chat history on Page #${page ?: 1}!"
            )
        }

        player.sendMessage(
            "${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(45)}",
            "${CC.GREEN}${it.name}'s Chat History:"
        )

        chatHistory.values.forEach { history ->
            val ago = System.currentTimeMillis() - history.timestamp.value
            val timestamp = if (ago >= Duration.ofDays(1L).toMillis())
            {
                TimeUtil.formatIntoCalendarString(history.timestamp.toDate())
            } else
            {
                "${TimeUtil.formatIntoAbbreviatedString(ago.toInt() / 1000)} ago"
            }

            player.sendMessage("${CC.GRAY}$timestamp${CC.WHITE}: ${history.message}")
        }

        val toolbar = FancyMessage()
        toolbar.withMessage("${CC.YELLOW}<<")
            .andHoverOf("Visit page ${(page ?: 1) - 1}")
            .andCommandOf(
                ClickEvent.Action.RUN_COMMAND,
                "/chathistory ${it.name} ${(page ?: 1) - 1}"
            )

        toolbar.withMessage(" ${CC.GRAY}Page #${page ?: 1} ")

        toolbar.withMessage("${CC.YELLOW}>>")
            .andHoverOf("Visit page ${(page ?: 1) + 1}")
            .andCommandOf(
                ClickEvent.Action.RUN_COMMAND,
                "/chathistory ${it.name} ${(page ?: 1) + 1}"
            )

        player.sendMessage("")
        toolbar.sendToPlayer(player.bukkit())
        player.sendMessage("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(45)}")
    }

    fun preLoadChatHistory(playerID: UUID, pageNumber: Int = 1, pageSize: Int = 5) = DataStoreObjectControllerCache
        .findNotNull<MessageAuditLog>()
        .mongo()
        .loadFilteredSync {
            filter(Filters.eq("playerID", playerID.toString()))
                .sort(Sorts.descending("timestamp"))
                .skip(pageSize * (pageNumber - 1))
                .limit(pageSize)
        }
}
