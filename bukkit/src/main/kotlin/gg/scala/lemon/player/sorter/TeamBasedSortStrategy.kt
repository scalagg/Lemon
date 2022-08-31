package gg.scala.lemon.player.sorter

import gg.scala.commons.command.ScalaCommand
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import me.lucko.helper.Events
import me.lucko.helper.utils.Players
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scoreboard.Team
import java.util.*

/**
 * @author GrowlyX
 * @since 8/31/2022
 */
@Service
object TeamBasedSortStrategy : ScalaCommand()
{
    @Inject
    lateinit var plugin: Lemon

    @Configure
    fun configure()
    {
        if (!plugin.settings.tablistSortingEnabled)
        {
            plugin.logger.info("Skipping tablist sorting initialization.")
            return
        }

        listOf(
            PlayerJoinEvent::class.java,
            PlayerQuitEvent::class.java
        ).forEach {
            Events.subscribe(it)
                .handler { event ->
                    if (event is PlayerJoinEvent)
                    {
                        Tasks.delayed(5L)
                        {
                            orderTabList()
                        }
                    } else
                    {
                        orderTabList()
                    }
                }
        }
    }

    private fun orderTabList()
    {
        for (player in Players.all())
        {
            val scoreboard = player.scoreboard
                ?: plugin.server
                    .scoreboardManager.newScoreboard

            val teamMappings = mutableMapOf<UUID, Team>()

            RankHandler.ranks.entries
                .sortedByDescending { it.value.weight }
                .forEachIndexed { index, entry ->
                    teamMappings[entry.key] = scoreboard.getTeam(index.toString())
                        ?: scoreboard.registerNewTeam(index.toString())
                }

            for (other in Players.all())
            {
                val lemonPlayer = PlayerHandler
                    .find(other.uniqueId) ?: continue

                val rank = lemonPlayer
                    .activeGrant?.getRank()?.uuid
                    ?: RankHandler.getDefaultRank().uuid

                val mapping = teamMappings[rank]
                    ?: teamMappings.values.last()

                mapping.addEntry(other.name)
            }
        }
    }
}
