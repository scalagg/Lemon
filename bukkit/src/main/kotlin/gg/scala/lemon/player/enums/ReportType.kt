package gg.scala.lemon.player.enums

import com.cryptomorin.xseries.XMaterial

/**
 * @author GrowlyX
 * @since 9/6/2021
 */
enum class ReportType(
    val fancyName: String,
    val examples: List<String>,
    val material: XMaterial,
) {

    COMBAT_HACKS(
        "Combat Hacks",
        listOf("KillAura", "Reach", "Aim Assist"),
        XMaterial.DIAMOND_SWORD
    ),
    MOVEMENT_HACKS(
        "Movement Hacks",
        listOf("Speed", "Bunny Hop", "Fly"),
        XMaterial.RABBIT_FOOT
    ),
    VELOCITY_HACKS(
        "Velocity Hacks",
        listOf("Velocity", "Reduced KB", "Anti KB"),
        XMaterial.SLIME_BALL
    ),
    CHAT_ABUSE(
        "Chat Abuse",
        listOf("Toxicity", "Spam", "Illegal Characters"),
        XMaterial.PAPER
    ),
    GAME_SABOTAGE(
        "Game Sabotage",
        listOf("Camping", "Running", "Stalling"),
        XMaterial.GLOWSTONE_DUST
    ),
    CROSS_TEAMING(
        "Teaming",
        listOf("Trucing", "Player Collision", "Group Game Sabotage"),
        XMaterial.STICK
    ),
    SUSPICIOUS_ACTIVITY(
        "Suspicious Activity",
        listOf("DDoS Threats", "Dox Threats", "Self Harm"),
        XMaterial.RED_DYE
    ),
    OTHER(
        "Other",
        listOf("Anything that's not listed..."),
        XMaterial.PAINTING
    );

    companion object {
        @JvmStatic
        val VALUES = values()
    }

}
