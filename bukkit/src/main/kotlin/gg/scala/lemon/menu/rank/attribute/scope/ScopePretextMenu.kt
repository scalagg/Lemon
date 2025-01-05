package gg.scala.lemon.menu.rank.attribute.scope

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.menu.rank.attribute.scope.group.ScopeGroupSpecificSelectionMenu
import gg.scala.lemon.menu.rank.attribute.scope.server.ScopeServerSpecificPretextMenu
import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * Class created on 1/2/2025

 * @author Max C.
 * @project lemon
 * @website https://solo.to/redis
 */
class ScopePretextMenu(val rank: Rank): Menu()
{
    init
    {
        placeholder = true
    }

    override fun getTitle(player: Player): String = "Select an Option"

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            it[3] = ItemBuilder.of(XMaterial.CHEST)
                .name("${CC.GREEN}Servers")
                .addToLore("${CC.GRAY}Left-Click to edit the server-specific scopes of this rank")
                .toButton { _, _ ->
                    ScopeServerSpecificPretextMenu(rank).openMenu(player)
                }

            it[5] = ItemBuilder.of(XMaterial.ENDER_CHEST)
                .name("${CC.GREEN}Groups")
                .addToLore("${CC.GRAY}Left-Click to edit the group-specific scopes of this rank")
                .toButton { _, _ ->
                    ScopeGroupSpecificSelectionMenu(rank).openMenu(player)
                }
        }
    }
}