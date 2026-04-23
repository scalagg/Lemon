package gg.scala.lemon.menu.server

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.lemon.menu.punishment.PunishmentDetailedViewMenu
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

/**
 * @author Topu
 * @date 3/31/2025
 */
class ServerViewMenu : PaginatedMenu() {

    init {
        autoUpdate = true
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player) = "Servers"

    override fun getMaxItemsPerPage(player: Player) = PunishmentDetailedViewMenu.SLOTS.size - 1
    override fun getAllPagesButtonSlots() = PunishmentDetailedViewMenu.SLOTS

    override fun size(buttons: Map<Int, Button>) = 36

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()

        ServerContainer.allServers().forEach { (_, server) ->
            val gameServer = server as GameServer
            buttons[buttons.size] = ItemBuilder.of(XMaterial.EMERALD_BLOCK)
                .name("${CC.GREEN}${gameServer.id}")
                .addToLore(
                    "",
                    "${CC.GRAY}Region: ${CC.WHITE}${gameServer.region}",
                    "${CC.GRAY}Groups: ${CC.WHITE}${gameServer.groups.joinToString(", ")}",
                    "${CC.GRAY}Software: ${CC.WHITE}${gameServer.getSoftware() ?: "Unknown"}",
                    "",
                    "${CC.GREEN}Performance:",
                    "${CC.GRAY}Players: ${CC.WHITE}${gameServer.getPlayersCount() ?: "N/A"} ${CC.GRAY}${Constants.THIN_VERTICAL_LINE} ${CC.WHITE}${gameServer.getMaxPlayers() ?: "N/A"}",
                    "${CC.GRAY}TPS: ${CC.GREEN}${
                        gameServer.getTPS()?.let { if (it > 20.0) "*20.0" else "%.1f".format(it) } ?: "N/A"
                    }",
                    "${CC.GRAY}CPU Usage: ${CC.WHITE}${gameServer.getCpuUsage()?.let { "%.1f%%".format(it) } ?: "N/A"}",
                    "",
                    "${CC.GREEN}Whitelist:",
                    "${CC.GRAY}Status: ${CC.WHITE}${gameServer.getWhitelisted() ?: "Unknown"}",
                    "${CC.GRAY}List: ${CC.WHITE}${gameServer.getWhitelistedPlayers()?.size ?: "N/A"}",
                    "",
                    "${CC.GREEN}Left-Click to connect",
                    "${CC.YELLOW}Right-Click to view metadata",
                    "${CC.GOLD}Shift-Click for additional actions"
                )
                .toButton { _, type ->
                    player.closeInventory()
                    when (type) {
                        ClickType.LEFT -> VelocityRedirectSystem.redirect(player, gameServer.id)
//                        ClickType.RIGHT -> ServerMetadataMenu(gameServer).openMenu(player)
//                        ClickType.SHIFT_LEFT -> ServerActionsMenu(gameServer).openMenu(player)
                        else -> return@toButton
                    }
                }
        }
        return buttons
    }
}