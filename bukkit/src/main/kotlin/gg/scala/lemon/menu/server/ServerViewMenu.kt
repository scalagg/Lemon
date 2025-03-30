package gg.scala.lemon.menu.server

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * Created by ScoreBoardAPI devscoreboardapi@gmail.com on 3/30/2025.
 */
class ServerViewMenu : Menu() {

    init {
        autoUpdate = true
    }

    override fun getTitle(player: Player): String {
        return "Live Servers"
    }

    override fun getMinSize(): Int {
        return 27
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        ServerContainer.allServers().forEach { (id, server) ->
            buttons[buttons.size] = ServerButton(server as GameServer)
        }
        return buttons
    }

    internal class ServerButton(val server: GameServer) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder(XMaterial.EMERALD_BLOCK)
                .name("${CC.GREEN}Server: ${CC.WHITE}${server.id} ")
                .addToLore(
                    "${CC.GRAY}Region: ${CC.WHITE}${server.region}",
                    "${CC.GRAY}Groups: ${CC.WHITE}${server.groups.joinToString(", ")}",
                    "${CC.GRAY}Players: ${CC.WHITE}${server.getPlayersCount()} | ${server.getMaxPlayers()}",
                    "${CC.GRAY}TPS: ${CC.WHITE}${server.getTPS()}",
                    "${CC.GRAY}Whitelisted: ${CC.WHITE}${server.getWhitelisted()}",
                    "",
                    "${CC.YELLOW}Right click to connect",
                    "${CC.YELLOW}Left click to view Metadata"
                )
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            VelocityRedirectSystem.redirect(player, server.id)
        }
    }
}
