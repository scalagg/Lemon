package com.solexgames.lemon.player.enums

import com.cryptomorin.xseries.XMaterial

/**
 * @author GrowlyX
 * @since 9/6/2021
 */
enum class ReportType(
    val fancyName: String,
    val examples: List<String>,
    val material: XMaterial,
    val data: Short
) {

    COMBAT_HACKS(
        "Combat Hacks",
        listOf("KillAura", "Reach", "Aim Assist"),
        XMaterial.DIAMOND_SWORD,
        0
    ),
    MOVEMENT_HACKS(
        "Movement Hacks",
        listOf("Speed", "Bunny Hop", "Fly"),
        XMaterial.RABBIT_FOOT,
        0
    ),
    VELOCITY_HACKS(
        "Velocity Hacks",
        listOf("Velocity", "Reduced KB", "Anti KB"),
        XMaterial.SLIME_BALL,
        0
    ),
    CHAT_ABUSE(
        "Chat Abuse",
        listOf("Toxicity", "Spam", "Illegal Characters"),
        XMaterial.PAPER,
        0
    ),
    GAME_SABOTAGE(
        "Game Sabotage",
        listOf("Camping", "Running", "Stalling"),
        XMaterial.RED_BED,
        0
    ),
    CROSS_TEAMING(
        "Teaming",
        listOf("Trucing", "Player Collision", "Group Game Sabotage"),
        XMaterial.STICK,
        0
    ),
    SUSPICIOUS_ACTIVITY(
        "Suspicious Activity",
        listOf("DDos Threats", "Dox Threats", "Self Harm"),
        XMaterial.RED_DYE,
        1
    ),
    OTHER(
        "Other",
        listOf("Anything that's not listed..."),
        XMaterial.PAINTING,
        0
    );

    companion object {
        @JvmStatic
        val VALUES = values()
    }

}
