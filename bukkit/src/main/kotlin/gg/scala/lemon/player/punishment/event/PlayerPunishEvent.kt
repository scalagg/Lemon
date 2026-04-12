package gg.scala.lemon.player.punishment.event

import gg.scala.commons.event.StatelessEvent
import gg.scala.lemon.player.punishment.Punishment
import org.bukkit.entity.Player
import java.util.UUID

data class PlayerPunishEvent(
    val player: UUID,
    val punishment: Punishment
) : StatelessEvent()
