package gg.scala.lemon.cooldown.impl

import gg.scala.lemon.cooldown.type.PlayerStaticCooldown

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
object CommandCooldown : PlayerStaticCooldown(
    "command", 1000L
)
