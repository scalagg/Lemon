package gg.scala.lemon.cooldown.type

import gg.scala.lemon.cooldown.Cooldown
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
abstract class PlayerStaticCooldown(
    id: String, private val duration: Long
) : PlayerCooldown(id)
{
    override fun durationFor(t: Player): Long
    {
        return duration
    }
}
