package gg.scala.lemon.cooldown.type

import gg.scala.lemon.cooldown.Cooldown
import org.bukkit.entity.Player

abstract class PlayerCooldown(
    private val id: String
) : Cooldown<Player>()
{
    override fun id(): String = id
}
