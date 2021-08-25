package com.solexgames.lemon.punishment.category

enum class PunishmentCategory(val infinitiveVersion: String, val intensity: PunishmentCategoryIntensity) {

    WARNING("warned", PunishmentCategoryIntensity.LIGHT),
    MUTE("muted", PunishmentCategoryIntensity.LIGHT),
    BAN("banned", PunishmentCategoryIntensity.MEDIUM),
    IP_RELATIVE_BAN("ip-banned", PunishmentCategoryIntensity.MEDIUM),
    BLACKLIST("blacklisted", PunishmentCategoryIntensity.MAX),

}
