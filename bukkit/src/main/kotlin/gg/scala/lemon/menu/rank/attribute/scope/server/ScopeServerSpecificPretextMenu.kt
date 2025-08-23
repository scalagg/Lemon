package gg.scala.lemon.menu.rank.attribute.scope.server

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.lemon.menu.rank.attribute.RankAttributeEditorMenu
import gg.scala.lemon.menu.rank.attribute.scope.ScopePretextMenu
import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player

/**
 * Class created on 1/2/2025

 * @author Max C.
 * @project lemon
 * @website https://solo.to/redis
 */
class ScopeServerSpecificPretextMenu(val rank: Rank): PaginatedMenu()
{
    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            getGroups().forEach { group ->
                it[it.size] = ItemBuilder.of(XMaterial.PAPER)
                    .name("${CC.GREEN}$group")
                    .addToLore("${CC.GRAY}Left-Click to select this group and view all servers")
                    .toButton { _, _ ->
                        ScopeServerSpecificSelectionMenu(rank, group).openMenu(player)
                    }
            }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose) {
            Tasks.sync {
                ScopePretextMenu(rank).openMenu(player)
            }
        }
    }

    override fun getPrePaginatedTitle(player: Player): String = "Select a Group!"

    private fun getGroups(): MutableSet<String> {
        val servers = ServerContainer.allServers<GameServer>()
        val groups = mutableSetOf<String>()

        servers.forEach {
            groups.addAll(it.groups)
        }

        return groups
    }
}