package gg.scala.lemon.testing.criteria.pagination

import gg.scala.lemon.server.ServerInstance
import net.evilblock.cubed.menu.pagination.CriteriaPaginatedMenu
import net.evilblock.cubed.menu.pagination.PaginationSort
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/14/2021
 */
class TestPaginationSort : PaginationSort<ServerInstance>
{
    override fun apply(
        player: Player,
        menu: CriteriaPaginatedMenu<ServerInstance>,
        src: Collection<ServerInstance>
    ): Collection<ServerInstance>
    {
        return src.filter { it.serverGroup == "practice" }
    }

    override fun name(): String = "Sort Practice Group"
}
