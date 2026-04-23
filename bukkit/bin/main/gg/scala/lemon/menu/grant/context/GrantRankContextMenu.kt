package gg.scala.lemon.menu.grant.context

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.menu.grant.context.scope.ScopeSelectionMenu
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
class GrantRankContextMenu(
    private val uuid: UUID,
    private val name: String,
    private val colored: String
) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "Granting for $colored${CC.D_GRAY}"
    }

    override fun getMaxItemsPerPage(player: Player) = 27

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return mutableMapOf<Int, Button>().also {
            val lemonPlayer = PlayerHandler
                .findPlayer(player)
                .orElse(null)

            RankHandler.sorted.forEach { rank ->
                val canUseForGrant = lemonPlayer != null &&
                        lemonPlayer.activeGrant!!.getRank().weight >= rank.weight &&
                        rank.grantable()

                val scopeSelectionActive = rank.scopes().isNotEmpty() &&
                        player.hasPermission("lemon.command.grant.scopeselection")

                it[it.size] = ItemBuilder
                    .of(
                        if (canUseForGrant) Material.WOOL else Material.COAL_BLOCK
                    )
                    .data(
                        (if (canUseForGrant) ColorUtil.CHAT_COLOR_TO_WOOL_DATA[
                            ChatColor.getByChar(rank.color[1]) ?: ChatColor.WHITE
                        ]?.toByte() ?: 1 else 0).toShort()
                    )
                    .name(rank.getColoredName())
                    .addToLore(
                        "${CC.GRAY}Name: ${CC.WHITE}${rank.name}",
                        "${CC.GRAY}Weight: ${CC.WHITE}${
                            Numbers.format(rank.weight)
                        }",
                        "",
                        "${CC.GREEN}Metadata:",
                        "${CC.GRAY}Display Name: ${
                            if (rank.displayName == null) "${CC.RED}None" else "${CC.WHITE}${rank.displayName}"
                        }",
                        "${CC.GRAY}Prefix: ${CC.WHITE}${
                            QuickAccess.replaceEmpty(rank.prefix)
                        }",
                        "${CC.GRAY}Suffix: ${CC.WHITE}${
                            QuickAccess.replaceEmpty(rank.suffix)
                        }",
                        "",
                        "${CC.GRAY}Visible: ${
                            if (rank.visible) "${CC.GREEN}Yes" else "${CC.RED}No"
                        }",
                        "${CC.GRAY}Grantable: ${
                            if (rank.grantable()) "${CC.GREEN}Yes" else "${CC.RED}No"
                        }",
                        ""
                    )
                    .apply {
                        if (canUseForGrant)
                        {
                            addToLore("${CC.GREEN}${
                                if (scopeSelectionActive) "Left-click" else "Click"
                            } to grant this rank!")

                            if (scopeSelectionActive)
                            {
                                addToLore("${CC.GOLD}Right-click to select scopes.")
                            }
                        } else
                        {
                            addToLore("${CC.RED}You cannot grant this rank.")
                        }
                    }
                    .toButton { _, type ->
                        if (canUseForGrant) {
                            if (type!!.isRightClick)
                            {
                                if (scopeSelectionActive)
                                {
                                    ScopeSelectionMenu(uuid, name, colored, rank).openMenu(player)
                                    return@toButton
                                }
                            }

                            GrantDurationContextMenu(uuid, name, rank, colored).openMenu(player)
                        }
                    }
            }
        }
    }
}
