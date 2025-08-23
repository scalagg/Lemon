package gg.scala.lemon.player.punishment

import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.template.PunishmentSeverity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks offenses for players across different categories and severities
 * @author Pawful
 */
object OffenseTracker {
    private val offenses = ConcurrentHashMap<UUID, MutableMap<String, Int>>()

    /**
     * Get the offense count for a player in a specific category and severity
     */
    fun getOffenseCount(uuid: UUID, category: PunishmentCategory, severity: PunishmentSeverity): Int {
        return offenses.getOrPut(uuid) { mutableMapOf() }
            .getOrDefault(getKey(category, severity), 0)
    }

    /**
     * Increment the offense count for a player in a specific category and severity
     */
    fun incrementOffense(uuid: UUID, category: PunishmentCategory, severity: PunishmentSeverity) {
        val playerOffenses = offenses.getOrPut(uuid) { mutableMapOf() }
        val key = getKey(category, severity)
        playerOffenses[key] = playerOffenses.getOrDefault(key, 0) + 1
    }

    /**
     * Reset all offenses for a player
     */
    fun resetOffenses(uuid: UUID) {
        offenses.remove(uuid)
    }

    /**
     * Reset offenses for a specific category and severity
     */
    fun resetOffenses(uuid: UUID, category: PunishmentCategory, severity: PunishmentSeverity) {
        offenses.get(uuid)?.remove(getKey(category, severity))
    }

    private fun getKey(category: PunishmentCategory, severity: PunishmentSeverity): String {
        return "${category.name}_${severity.name}"
    }
} 