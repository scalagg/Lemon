package gg.scala.lemon.filter.ml

import gg.scala.lemon.util.QuickAccess.username
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import java.time.Duration

/**
 * @author GrowlyX
 * @since 8/23/2024
 */
class ChatMLPunishmentAuditReviewMenu(private val entries: List<ChatMLPunishmentAudit>) : PaginatedMenu()
{
    override fun getAllPagesButtons(player: Player) = entries
        .map {
            ItemBuilder
                .of(Material.MAP)
                .name("${CC.GREEN}${it.target.username()}")
                .addToLore(
                    "${CC.GRAY}Server:",
                    "${CC.WHITE}${it.fromServer}",
                    "",
                    "${CC.GRAY}Prediction:",
                    "${CC.RED}${it.prediction}",
                    "",
                    "${CC.GRAY}Context:"
                )
                .apply {
                    it.chatContext.forEach { history ->
                        val ago = System.currentTimeMillis() - history.timestamp.value
                        val timestamp = if (ago >= Duration.ofDays(1L).toMillis())
                        {
                            TimeUtil.formatIntoCalendarString(history.timestamp.toDate())
                        } else
                        {
                            "${TimeUtil.formatIntoAbbreviatedString(ago.toInt() / 1000)} ago"
                        }

                        addToLore("${CC.D_GRAY}$timestamp${CC.WHITE}: ${history.message}")
                    }
                }
                .addToLore(
                    "",
                    "${CC.GREEN}Click to unmute!"
                )
                .toButton { _, _ ->
                    player.performCommand("unmute ${it.target} ChatML False Prediction -s")
                }
        }
        .withIndex()
        .associate { it.index to it.value }

    override fun getPrePaginatedTitle(player: Player) = "Reviewing ChatML..."
}
