package gg.scala.lemon.cooldown.type

import gg.scala.lemon.Lemon
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
abstract class PlayerStaticCooldown(
    id: String, private val duration: Long
) : PlayerCooldown(id)
{
    val tasks = mutableMapOf<UUID, BukkitTask>()

    var shouldNotify = false
    var onExpiration: (Player) -> Unit = {}

    fun notifyOnExpiration(): PlayerStaticCooldown
    {
        shouldNotify = true
        return this
    }

    fun whenExpired(lambda: (Player) -> Unit): PlayerStaticCooldown
    {
        onExpiration = lambda
        return this
    }

    override fun addOrOverride(t: Player)
    {
        super.addOrOverride(t)

        if (shouldNotify)
        {
            tasks[t.uniqueId] = Bukkit.getScheduler().runTaskLater(
                Lemon.instance, CooldownNotificationTask(t.uniqueId),
                (durationFor(t) / 1000L) * 20L
            )
        }
    }

    override fun reset(t: Player)
    {
        if (shouldNotify)
        {
            tasks.remove(t.uniqueId)?.cancel()
        }

        super.reset(t)
    }

    override fun durationFor(t: Player): Long
    {
        return duration
    }

    inner class CooldownNotificationTask(
        private val uniqueId: UUID
    ) : Runnable
    {

        override fun run()
        {
            val bukkitPlayer = Bukkit.getPlayer(uniqueId)

            if (bukkitPlayer != null)
            {
                onExpiration.invoke(bukkitPlayer)
                tasks.remove(uniqueId)
            }
        }
    }
}
