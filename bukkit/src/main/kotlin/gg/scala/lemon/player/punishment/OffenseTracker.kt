package gg.scala.lemon.player.punishment

import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.template.PunishmentSeverity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object OffenseTracker {
    private val offenses = ConcurrentHashMap<UUID, MutableMap<String, Int>>()

    fun getOffenseCount(uuid: UUID, category: PunishmentCategory, severity: PunishmentSeverity): Int {
        return offenses.getOrPut(uuid) { mutableMapOf() }
            .getOrDefault(getKey(category, severity), 0)
    }

    fun incrementOffense(uuid: UUID, category: PunishmentCategory, severity: PunishmentSeverity) {
        val playerOffenses = offenses.getOrPut(uuid) { mutableMapOf() }
        val key = getKey(category, severity)
        playerOffenses[key] = playerOffenses.getOrDefault(key, 0) + 1
    }

    fun resetOffenses(uuid: UUID) {
        offenses.remove(uuid)
    }

    fun resetOffenses(uuid: UUID, category: PunishmentCategory, severity: PunishmentSeverity) {
        offenses.get(uuid)?.remove(getKey(category, severity))
    }

    private fun getKey(category: PunishmentCategory, severity: PunishmentSeverity): String {
        return "${category.name}_${severity.name}"
    }
} 