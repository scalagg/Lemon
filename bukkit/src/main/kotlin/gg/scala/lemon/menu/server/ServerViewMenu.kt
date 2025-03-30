package gg.scala.lemon.menu.server

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.agnostic.sync.server.ServerContainer
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.entity.Player

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

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        ServerContainer.allServers().forEach { (id, server) ->
            buttons[buttons.size + 1] = ItemBuilder(XMaterial.EMERALD_BLOCK)
                .name("${CC.GREEN}Server: ${ChatColor.WHITE}${server.id}")
                .addToLore(
                    "${CC.GRAY}Region: ${ChatColor.WHITE}${server.region}",
                    "${CC.GRAY}Groups: ${ChatColor.WHITE}${server.groups.joinToString(", ")}",
                    "",
                    "${CC.YELLOW}Click to Join"
                ).toButton { _, _ ->
                    player.closeInventory()
                    player.performCommand("server ${server.id}")
                }
        }
        return buttons
    }
}
