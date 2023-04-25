package gg.scala.lemon.menu.grant.context.scope

import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.lemon.menu.grant.context.GrantDurationContextMenu
import gg.scala.lemon.menu.grant.context.GrantRankContextMenu
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.scope.ServerScope
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/8/2022
 */
class ScopeSelectionMenu(
    private val uuid: UUID,
    private val name: String,
    private val colored: String,
    private val rank: Rank,
    private val chosen: MutableList<String> = mutableListOf()
) : PaginatedMenu()
{
    init
    {
        updateAfterClick = true
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        buttons[4] = ItemBuilder
            .copyOf(
                object : TexturedHeadButton(Constants.GREEN_PLUS_TEXTURE){}
                    .getButtonItem(player)
            )
            .name("${CC.B_GREEN}Continue")
            .addToLore(
                "${CC.WHITE}Using the scopes:",
                "${CC.GREEN}${
                    if (chosen.isEmpty()) "None" else chosen.joinToString(", ")
                }",
                "",
                "${CC.YELLOW}Click to continue."
            )
            .toButton { _, _ ->
                if (chosen.isEmpty())
                {
                    player.sendMessage("${CC.RED}You have chosen no scopes. Please select at least 1 scope or use the normal grant process.")
                    return@toButton
                }

                GrantDurationContextMenu(uuid, name, rank, colored, chosen).openMenu(player)
            }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose)
        {
            Tasks.delayed(1L)
            {
                GrantRankContextMenu(uuid, name, colored).openMenu(player)
            }
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        rank.scopes()
            .map(ServerScope::group)
            .forEach {
                buttons[buttons.size] = ItemBuilder
                    .of(Material.WOOL)
                    .data(
                        if (chosen.contains(it)) 5 else 0
                    )
                    .name("${CC.WHITE}$it")
                    .toButton { _, _ ->
                        if (chosen.contains(it))
                        {
                            chosen.remove(it)
                            return@toButton
                        }

                        chosen.add(it)
                    }
            }

        return buttons
    }

    override fun getPrePaginatedTitle(player: Player) =
        "Scope selection for $colored${CC.D_GRAY}"
}
