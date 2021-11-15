package gg.scala.lemon.testing.criteria

import gg.scala.lemon.server.ServerInstance
import gg.scala.lemon.testing.criteria.pagination.TestPaginationFilter
import gg.scala.lemon.testing.criteria.pagination.TestPaginationSort
import net.evilblock.cubed.menu.pagination.CriteriaPaginatedMenu
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/14/2021
 */
class TestCriteriaMenu(
    private val instancesAsOfNow: List<ServerInstance>
) : CriteriaPaginatedMenu<ServerInstance>()
{
    override fun createItemButton(player: Player, item: ServerInstance) = TestFilteredItemButton(item)

    override fun getFilters(player: Player) = listOf(TestPaginationFilter())
    override fun getSorts(player: Player) = listOf(TestPaginationSort())

    override fun getPrePaginatedTitle(player: Player) = "Test Criteria"

    override fun getSourceSet(player: Player) = instancesAsOfNow

    inner class TestFilteredItemButton(item: ServerInstance) : FilteredItemButton<ServerInstance>(item)
}
