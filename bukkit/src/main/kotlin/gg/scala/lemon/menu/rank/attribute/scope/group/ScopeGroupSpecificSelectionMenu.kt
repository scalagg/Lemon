package gg.scala.lemon.menu.rank.attribute.scope.group

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

class ScopeGroupSpecificSelectionMenu(val rank: Rank) : Menu()
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
        val groups = mutableSetOf<String>()

        servers.forEach {
            groups.addAll(it.groups)
        }

        for (i in groups.indices)
        {
            buttons[i] = ServerDisplayButton(groups.elementAt(i), rank)
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

    internal class ServerDisplayButton(val server: String, val rank: Rank) : Button()
    {
        val contains = rank.scopes().any { it.group.equals(server, ignoreCase = true) }

        override fun getButtonItem(player: Player): ItemStack = ItemBuilder
            .of(
                if (contains) XMaterial.GREEN_WOOL else XMaterial.RED_WOOL
            )
            .name("${CC.YELLOW}$server")
            .setLore(generateLore())
            .build()

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            if (!contains)
            {
                rank.scopes().add(ServerScope(server, mutableListOf()))
            } else
            {
                rank.serverScopes?.removeIf { it.group.equals(server, ignoreCase = true) }
            }

            rank.saveAndPushUpdatesGlobally()
        }

        private fun generateLore(): MutableList<String>
        {
            val lore = mutableListOf<String>()

            lore.add(if (contains) "${CC.GREEN}(Active)" else "${CC.RED}(Inactive)")
            lore.add(" ")
            lore.add("${CC.GREEN}Servers In Group:")

            val serversInGroup = ServerContainer.getServersInGroup(server)

            serversInGroup.forEach {
                lore.add("${CC.GRAY}- ${CC.WHITE}${it.id}")
            }
            lore.add(" ")
            lore.add(if (contains) "${CC.RED}Left-Click to remove this group from the current scope list" else "${CC.GREEN}Left-Click to add this group to the scope list")

            return lore
        }
    }
}