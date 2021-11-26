package gg.scala.lemon.player.entity.superboat

import gg.scala.lemon.player.entity.EntityHider
import net.evilblock.cubed.entity.Entity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Boat
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/25/2021
 */
class EntitySuperBoat(
    location: Location,
    private val amount: Int,
    private val target: Player
) : Entity(location)
{
    companion object
    {
        @JvmStatic
        val HIDER = EntityHider(
            policy = EntityHider.Policy.BLACKLIST
        )
    }

    private val entities = mutableListOf<Boat>()

    override fun getTypeName() = "super_boat"

    override fun spawn(player: Player)
    {
        val world = Bukkit.getWorlds()[0]

        for (i in 0..amount)
        {
            val entity = world.spawn(
                location, Boat::class.java
            ).also { boat ->
                Bukkit.getOnlinePlayers().forEach {
                    if (it != target)
                    {
                        HIDER.hideEntity(it, boat)
                    }
                }
            }

            entities.add(entity)
        }
    }

    override fun destroy(player: Player)
    {
        entities.forEach {
            it.remove()
        }
    }
}