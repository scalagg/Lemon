package com.solexgames.lemon.player.punishment.category

enum class PunishmentCategory(val infinitiveVersion: String, var fancyVersion: String, var instant: Boolean, val intensity: PunishmentCategoryIntensity) {

    KICK("kicked", "Kick", true, PunishmentCategoryIntensity.LIGHT),
    MUTE("muted", "Mute", false, PunishmentCategoryIntensity.LIGHT),
    BAN("banned", "Ban", false, PunishmentCategoryIntensity.MEDIUM),
    BLACKLIST("blacklisted", "Blacklist", false, PunishmentCategoryIntensity.MAX),

}
