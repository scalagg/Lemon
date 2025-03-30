package gg.scala.lemon.menu.server

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
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
                .name("${CC.GREEN}Server: ${CC.WHITE}${server.id} ")
                .addToLore(
                    "${CC.GRAY}Region: ${CC.WHITE}${server.region}",
                    "${CC.GRAY}Groups: ${CC.WHITE}${server.groups.joinToString(", ")}",


                    //todo put into metadata menu this is ugly and was just testing out menu and command api ect
                    "${CC.GRAY}Players: ${CC.WHITE}${server.getMetadata("server", "online").toString()} | ${server.getMetadata("server", "online-max").toString()}",
                    "${CC.GRAY}TPS: ${CC.WHITE}${server.getMetadata("server", "tps").toString()}",
                    "${CC.GRAY}Host: ${CC.WHITE}${server.getMetadata("server", "host").toString()} | ${server.getMetadata("server", "port").toString()}",
                    "${CC.GRAY}Whitelisted: ${CC.WHITE}${server.getMetadata("server", "whitelisted").toString()}",
                    "",

                    "${CC.YELLOW}Right click to connect",
                    "${CC.YELLOW}Left click to view Metadata"

                ).toButton { _, _ ->
                    player.closeInventory()
                    player.performCommand("server ${server.id}")
                }
        }
        return buttons
    }
}
