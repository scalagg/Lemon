package com.solexgames.lemon.player.punishment.category

enum class PunishmentCategory(val infinitiveVersion: String, var fancyVersion: String, var instant: Boolean, val intensity: PunishmentCategoryIntensity) {

    WARNING("warned", "Warning", false, PunishmentCategoryIntensity.LIGHT),
    KICK("kicked", "Kick", true, PunishmentCategoryIntensity.LIGHT),
    MUTE("muted", "Mute", false, PunishmentCategoryIntensity.LIGHT),
    BAN("banned", "Ban", false, PunishmentCategoryIntensity.MEDIUM),
    IP_RELATIVE_BAN("ip-banned", "IP-Ban", false, PunishmentCategoryIntensity.MEDIUM),
    BLACKLIST("blacklisted", "Blacklist", false, PunishmentCategoryIntensity.MAX),

}
