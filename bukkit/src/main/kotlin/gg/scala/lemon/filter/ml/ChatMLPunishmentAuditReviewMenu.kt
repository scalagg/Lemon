package gg.scala.lemon.filter.ml

import gg.scala.lemon.util.QuickAccess.username
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

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
                    addToLore(*it.chatContext.map { chat -> "${CC.WHITE}$chat" }.toTypedArray())
                }
                .addToLore(
                    "",
                    "${CC.GREEN}Click to unmute!"
                )
                .toButton { _, _ ->
                    player.performCommand("/unmute ${it.target} ChatML False Prediction -s")
                }
        }
        .withIndex()
        .associate { it.index to it.value }

    override fun getPrePaginatedTitle(player: Player) = "Reviewing ChatML..."
}
