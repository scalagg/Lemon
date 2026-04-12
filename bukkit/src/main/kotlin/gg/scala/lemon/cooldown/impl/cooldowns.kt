package gg.scala.lemon.cooldown.impl

import gg.scala.lemon.cooldown.type.PlayerStaticCooldown

object CommandCooldown : PlayerStaticCooldown(
    "command", 1000L
)
