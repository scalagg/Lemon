package gg.scala.lemon.menu.disguise

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 1/16/2022
 */
class DisguiseRankContextMenu : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val SLOTS = mutableListOf(
            1, 3, 5, 7, 10, 12, 14, 16
        )
    }

    override fun getPrePaginatedTitle(player: Player) =
        "Disguise ${Constants.DOUBLE_ARROW_RIGHT} Choose a rank"

    override fun getAllPagesButtonSlots() = SLOTS

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf()
    }


}
