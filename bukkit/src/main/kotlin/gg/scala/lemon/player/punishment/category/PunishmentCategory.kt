package gg.scala.lemon.player.punishment.category

import net.evilblock.cubed.util.CC

enum class PunishmentCategory(
    val inf: String,
    val color: String,
    var fancyVersion: String,
    var ing: String,
    var instant: Boolean,
    val intensity: PunishmentCategoryIntensity
)
{
    KICK("kicked", "", "Kick", "Kicking", true, PunishmentCategoryIntensity.MEDIUM),
    MUTE("muted", CC.I_WHITE, "Mute", "Muting", false, PunishmentCategoryIntensity.LIGHT),
    BAN("banned", CC.RED, "Ban", "Banning", false, PunishmentCategoryIntensity.MEDIUM),
    BLACKLIST("blacklisted", CC.D_RED, "Blacklist", "Blacklist", false, PunishmentCategoryIntensity.MEDIUM),
    IP_RELATIVE("", CC.ID_RED, "", "", false, PunishmentCategoryIntensity.MEDIUM);

    companion object
    {
        @JvmStatic
        val VALUES = values()

        @JvmStatic
        val PERSISTENT = listOf(MUTE, BAN, BLACKLIST)

        @JvmStatic
        val VALUES_2 = listOf(KICK, MUTE, BAN, BLACKLIST)

        @JvmStatic
        val IP_REL = listOf(BLACKLIST, BAN)

        @JvmStatic
        val WEIGHTED_DENIED = listOf(IP_RELATIVE, BLACKLIST, BAN)
    }
}
