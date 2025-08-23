package gg.scala.lemon.menu.rank.attribute.scope.server

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.lemon.menu.rank.attribute.RankAttributeEditorMenu
import gg.scala.lemon.menu.rank.attribute.scope.ScopePretextMenu
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.scope.ServerScope
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * Class created on 1/2/2025

 * @author Max C.
 * @project lemon
 * @website https://solo.to/redis
 */
class ScopeServerSpecificSelectionMenu(val rank: Rank, val group: String) : Menu()
{
    init
    {
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 36

    override fun getTitle(player: Player): String = "Editing Scopes..."

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose) {
            Tasks.sync {
                ScopePretextMenu(rank).openMenu(player)
            }
        }
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        for (int in 18 until 36)
        {
            buttons[int] = Button.placeholder(XMaterial.BLACK_STAINED_GLASS_PANE.parseMaterial()!!)
        }

        val servers = ServerContainer.allServers<GameServer>()
            .filter { it.groups.contains(group) }

        for (i in servers.indices)
        {
            buttons[i] = ServerDisplayButton(servers[i], rank, group)
        }

        buttons[30] = ItemBuilder.of(XMaterial.BARRIER)
            .name("${CC.RED}Clear All Scopes")
            .toButton { _, _ ->
                rank.scopes().clear()
                rank.saveAndPushUpdatesGlobally()
            }

        buttons[32] = ItemBuilder.of(XMaterial.ARROW)
            .name("${CC.RED}Go Back")
            .toButton { _, _ ->
                ScopePretextMenu(rank).openMenu(player)
            }

        return buttons
    }

    internal class ServerDisplayButton(val server: GameServer, val rank: Rank, val group: String) : Button()
    {
        val contains = rank.scopes().any { it.individual.contains(server.id) }

        override fun getButtonItem(player: Player): ItemStack = ItemBuilder
            .of(
                if (contains) XMaterial.GREEN_WOOL else XMaterial.RED_WOOL
            )
            .name("${CC.YELLOW}${server.id}")
            .setLore(generateLore())
            .build()

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            val scopes = rank.scopes().filter { it.group.equals(group, ignoreCase = true) }
                .toMutableList()

            if (scopes.isEmpty())
            {
                scopes.add(ServerScope(group).also { rank.scopes().add(it) })
            }

            val contains = scopes.any { it.individual.contains(server.id) }

            if (!contains)
            {
                scopes.forEach { if (!it.individual.contains(server.id)) it.individual.add(server.id) }
            } else
            {
                scopes.forEach { if (it.individual.contains(server.id)) it.individual.remove(server.id) }
            }

            rank.saveAndPushUpdatesGlobally()
        }

        private fun generateLore(): MutableList<String>
        {
            val lore = mutableListOf<String>()

            lore.add(if (contains) "${CC.GREEN}(Active)" else "${CC.RED}(Inactive)")
            lore.add(" ")
            lore.add(if (contains) "${CC.RED}Left-Click to remove this server from the current scope list" else "${CC.GREEN}Left-Click to add this server to the scope list")

            return lore
        }
    }
}