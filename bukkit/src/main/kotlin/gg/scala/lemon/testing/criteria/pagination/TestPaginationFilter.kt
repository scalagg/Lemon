package gg.scala.lemon.testing.criteria.pagination

import gg.scala.lemon.server.ServerInstance
import net.evilblock.cubed.menu.pagination.CriteriaPaginatedMenu
import net.evilblock.cubed.menu.pagination.PaginationFilter
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/14/2021
 */
class TestPaginationFilter : PaginationFilter<ServerInstance>
{
    override fun apply(
        player: Player,
        menu: CriteriaPaginatedMenu<ServerInstance>,
        list: Collection<ServerInstance>
    ): Collection<ServerInstance>
    {
        return list.filter { it.serverGroup == "hub" }
    }

    override fun name(): String = "Hub Group"
    override fun priority(): Int = 10
}
