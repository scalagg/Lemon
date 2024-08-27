package gg.scala.common.metadata

/**
 * @author GrowlyX
 * @since 8/27/2024
 */
data class LanguageProperties(
    var banMessageTemporary: Set<String> = setOf(
        "&e&l(S)&e You are banned from {serverName} &e&l(S)",
        "&cYou were banned for &f{duration}&c: &7%{reason} (#{id})",
        "&7Join {discord} to appeal!"
    ),
    var banMessagePermanent: Set<String> = setOf(
        "&e&l(S)&e You are banned from {serverName} &e&l(S)",
        "&cYou were banned for: &7%{reason} (#{id})",
        "&7Join {discord} to appeal!"
    ),
    var blacklistMessage: Set<String> = setOf(
        "&e&l(S)&e You are blacklisted from {serverName} &e&l(S)",
        "&7You may not appeal this type of punishment."
    ),
    var banRelationMessage: Set<String> = setOf(
        "&e&l(S)&e You are banned from {serverName} &e&l(S)",
        "&cYou were banned in relation to the user: &f{relationUser}",
        "&7Join {discord} to appeal!"
    ),
    var blacklistRelationMessage: Set<String> = setOf(
        "&e&l(S)&e You are blacklisted from {serverName} &e&l(S)",
        "&7Your blacklist is in relation to the account: &f{relationUser}"
    ),
    var muteMessage: Set<String> = setOf(
        "&e&l(S)&e {serverName} &e&l(S)",
        "&7{action} muted!",
        "",
        "&cReason: &f{reason}",
        "&cThis punishment will {durationExplanation}",
    ),
    var warnMessage: Set<String> = setOf(
        "&e&l(S)&e {serverName} &e&l(S)",
        "&cYou have been warned!",
        "&f{reason}"
    ),
    var kickMessage: Set<String> = setOf(
        "&e&l(S)&e {serverName} &e&l(S)",
        "&cYou have been kicked!",
        "&f{reason}"
    ),
    var cooldownDenyMessageAddition: String = "Purchase &3VIP&c rank or higher to bypass this!"
)

fun Set<String>.localize(vararg replacements: Pair<String, String>) = map {
    var localized = it
    replacements.forEach { localization -> localized = localized.replace("{${localization.first}}", localization.second) }
    localized
}.toSet()
