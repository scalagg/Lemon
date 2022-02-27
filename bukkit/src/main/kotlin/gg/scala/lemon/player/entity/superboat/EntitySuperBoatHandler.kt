package gg.scala.lemon.player.entity.superboat

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import me.lucko.helper.Events
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * @author GrowlyX
 * @since 11/25/2021
 */
@Service(name = "superboat")
object EntitySuperBoatHandler
{
    private val superBoats = mutableMapOf<UUID, EntitySuperBoat>()

    fun hasSuperBoat(player: Player) = superBoats.contains(player.uniqueId)
    fun getSuperBoat(player: Player) = superBoats[player.uniqueId]

    fun setupAndDisplaySuperBoat(player: Player, superBoat: EntitySuperBoat)
    {
        superBoat.initializeData()
        superBoat.spawn(player)

        superBoats[player.uniqueId] = superBoat
    }

    fun destroySuperBoatOf(player: Player)
    {
        superBoats.remove(player.uniqueId)?.destroy(player)
    }

    @Configure
    fun configure()
    {
        Events.subscribe(PlayerQuitEvent::class.java)
            .handler { destroySuperBoatOf(it.player) }
    }
}
