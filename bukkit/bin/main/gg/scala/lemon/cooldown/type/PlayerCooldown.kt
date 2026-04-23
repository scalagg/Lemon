package gg.scala.lemon.cooldown.type

import gg.scala.lemon.cooldown.Cooldown
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
abstract class PlayerCooldown(
    private val id: String
) : Cooldown<Player>()
{
    override fun id(): String = id
}
