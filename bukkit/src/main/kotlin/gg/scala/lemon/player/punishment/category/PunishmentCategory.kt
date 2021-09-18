package gg.scala.lemon.player.punishment.category

enum class PunishmentCategory(val inf: String, var fancyVersion: String, var ing: String, var instant: Boolean, val intensity: PunishmentCategoryIntensity) {

    KICK("kicked", "Kick", "Kicking", true, PunishmentCategoryIntensity.MEDIUM),
    MUTE("muted", "Mute", "Muting", false, PunishmentCategoryIntensity.LIGHT),
    BAN("banned", "Ban", "Banning", false, PunishmentCategoryIntensity.MEDIUM),
    BLACKLIST("blacklisted", "Blacklist", "Blacklist", false, PunishmentCategoryIntensity.MEDIUM);

    companion object {
        @JvmStatic
        val VALUES = values()

        @JvmStatic
        val PERSISTENT = listOf(MUTE, BAN, BLACKLIST)
    }

}
