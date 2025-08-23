package gg.scala.lemon.player.punishment.template

import gg.scala.lemon.player.punishment.category.PunishmentCategory
import java.util.concurrent.TimeUnit

/**
 * Represents a punishment template with predefined severity, reason, and duration
 * @author Pawful
 */
data class PunishmentTemplate(
    val category: PunishmentCategory,
    val severity: PunishmentSeverity,
    val reason: String,
    val duration: Long, // -1 for permanent
    val requiresAppeal: Boolean = false
) {
    companion object {
        // Chat Violations
        val CHAT_SPAM = PunishmentTemplate(
            PunishmentCategory.MUTE,
            PunishmentSeverity.LOW,
            "Chat Spam",
            TimeUnit.HOURS.toMillis(1)
        )
        val CHAT_CAPS = PunishmentTemplate(
            PunishmentCategory.MUTE,
            PunishmentSeverity.LOW,
            "Excessive Caps",
            TimeUnit.HOURS.toMillis(1)
        )
        val CHAT_SWEARING = PunishmentTemplate(
            PunishmentCategory.MUTE,
            PunishmentSeverity.MEDIUM,
            "Inappropriate Language",
            TimeUnit.HOURS.toMillis(2)
        )
        val CHAT_DISRESPECT = PunishmentTemplate(
            PunishmentCategory.MUTE,
            PunishmentSeverity.MEDIUM,
            "Disrespectful Behavior",
            TimeUnit.HOURS.toMillis(4)
        )
        val CHAT_HARASSMENT = PunishmentTemplate(
            PunishmentCategory.MUTE,
            PunishmentSeverity.HIGH,
            "Harassment",
            TimeUnit.DAYS.toMillis(1),
            requiresAppeal = true
        )
        val CHAT_HATE_SPEECH = PunishmentTemplate(
            PunishmentCategory.MUTE,
            PunishmentSeverity.EXTREME,
            "Hate Speech",
            TimeUnit.DAYS.toMillis(7),
            requiresAppeal = true
        )

        // Gameplay Violations
        val GAMEPLAY_SPAM = PunishmentTemplate(
            PunishmentCategory.MUTE,
            PunishmentSeverity.LOW,
            "Gameplay Spam",
            TimeUnit.HOURS.toMillis(1)
        )
        val GAMEPLAY_TEAMING = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.MEDIUM,
            "Teaming",
            TimeUnit.DAYS.toMillis(1)
        )
        val GAMEPLAY_CROSS_TEAMING = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.HIGH,
            "Cross Teaming",
            TimeUnit.DAYS.toMillis(3),
            requiresAppeal = true
        )
        val GAMEPLAY_TARGETING = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.HIGH,
            "Targeting",
            TimeUnit.DAYS.toMillis(3),
            requiresAppeal = true
        )

        // Client Violations
        val CLIENT_HACKS = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.EXTREME,
            "Hacking",
            -1L, // Permanent
            requiresAppeal = true
        )
        val CLIENT_XRAY = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.HIGH,
            "X-Ray",
            TimeUnit.DAYS.toMillis(14),
            requiresAppeal = true
        )
        val CLIENT_AUTOCLICKER = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.HIGH,
            "Auto Clicker",
            TimeUnit.DAYS.toMillis(14),
            requiresAppeal = true
        )
        val CLIENT_ANTIKB = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.HIGH,
            "Anti-Knockback",
            TimeUnit.DAYS.toMillis(14),
            requiresAppeal = true
        )

        // Behavior Violations
        val BEHAVIOR_DISRESPECT = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.MEDIUM,
            "Disrespectful Behavior",
            TimeUnit.DAYS.toMillis(1)
        )
        val BEHAVIOR_THREATS = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.HIGH,
            "Threats",
            TimeUnit.DAYS.toMillis(7),
            requiresAppeal = true
        )
        val BEHAVIOR_DDOS = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.EXTREME,
            "DDoS Threats",
            -1L, // Permanent
            requiresAppeal = true
        )
        val BEHAVIOR_IMPERSONATION = PunishmentTemplate(
            PunishmentCategory.BAN,
            PunishmentSeverity.HIGH,
            "Staff Impersonation",
            TimeUnit.DAYS.toMillis(14),
            requiresAppeal = true
        )

        // Get all templates for a category
        fun getTemplatesForCategory(category: PunishmentCategory): List<PunishmentTemplate> {
            return listOf(
                CHAT_SPAM, CHAT_CAPS, CHAT_SWEARING, CHAT_DISRESPECT, 
                CHAT_HARASSMENT, CHAT_HATE_SPEECH, GAMEPLAY_SPAM, 
                GAMEPLAY_TEAMING, GAMEPLAY_CROSS_TEAMING, GAMEPLAY_TARGETING,
                CLIENT_HACKS, CLIENT_XRAY, CLIENT_AUTOCLICKER, CLIENT_ANTIKB,
                BEHAVIOR_DISRESPECT, BEHAVIOR_THREATS, BEHAVIOR_DDOS,
                BEHAVIOR_IMPERSONATION
            ).filter { it.category == category }
        }

        // Get all templates for a severity
        fun getTemplatesForSeverity(severity: PunishmentSeverity): List<PunishmentTemplate> {
            return listOf(
                CHAT_SPAM, CHAT_CAPS, CHAT_SWEARING, CHAT_DISRESPECT, 
                CHAT_HARASSMENT, CHAT_HATE_SPEECH, GAMEPLAY_SPAM, 
                GAMEPLAY_TEAMING, GAMEPLAY_CROSS_TEAMING, GAMEPLAY_TARGETING,
                CLIENT_HACKS, CLIENT_XRAY, CLIENT_AUTOCLICKER, CLIENT_ANTIKB,
                BEHAVIOR_DISRESPECT, BEHAVIOR_THREATS, BEHAVIOR_DDOS,
                BEHAVIOR_IMPERSONATION
            ).filter { it.severity == severity }
        }
    }
}

/**
 * Represents the severity level of a punishment
 */
enum class PunishmentSeverity(
    val color: String,
    val displayName: String
) {
    LOW("&a", "Low"),
    MEDIUM("&e", "Medium"),
    HIGH("&c", "High"),
    EXTREME("&4", "Extreme");

    companion object {
        fun fromString(name: String): PunishmentSeverity? {
            return values().find { it.name.equals(name, ignoreCase = true) }
        }
    }
} 