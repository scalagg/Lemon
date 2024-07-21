package gg.scala.lemon.processor

import net.evilblock.cubed.util.CC
import xyz.mkotb.configapi.Coloured
import xyz.mkotb.configapi.comment.Comment

/**
 * @author GrowlyX
 * @since 8/28/2021
 */
class LanguageConfigProcessor
{
    @Coloured
    val tempBanMessage =
        "${CC.RED}You are banned from Scala for %s.\n" +
                "${CC.RED}You were banned for: ${CC.GRAY}%s (#%s)\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg.\n"

    @Coloured
    val permBanMessage =
        "${CC.RED}You are permanently banned from Scala\n" +
                "${CC.RED}You were banned for: ${CC.GRAY}%s (#%s)\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg.\n"

    @Coloured
    val blacklistMessage =
        "${CC.RED}You are blacklisted from Scala\n" +
                "${CC.GRAY}You may not appeal this type of punishment.\n"

    @Coloured
    val banRelationTemporaryMessage =
        "${CC.RED}Your IP is temporarily banned from Scala\n" +
                "${CC.RED}Your ban is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg.\n"

    @Coloured
    val banRelationPermanentMessage =
        "${CC.RED}Your IP is permanently banned from Scala\n" +
                "${CC.RED}Your ban is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg.\n"

    @Coloured
    val blacklistRelationMessage =
        "${CC.RED}Your IP is permanently blacklisted from Scala\n" +
                "${CC.RED}Your blacklist is in relation to the account: ${CC.GRAY}%s\n"

    @Coloured
    @Comment(
        "The replacements for this mute message are as follows:",
        " - Tense: \"You've been/You're currently\"",
        " - Reason",
        " - Expiration: \"not expire/expire in <date>\"",
        " - Punish ID: Punishment ID",
    )
    val muteMessage = """
        ${CC.RED}%s muted for: ${CC.WHITE}%s
        ${CC.RED}This punishment will %s.
        ${CC.GRAY}Your punishment ID is %s.
    """.trimIndent()

    @Coloured
    val kickMessage = """
        ${CC.RED}You've been kicked from %s:
        ${CC.WHITE}%s
    """.trimIndent()

    @Coloured
    val warnMessage = """
        ${CC.B_RED}You've been warned!
        ${CC.RED}Reason: ${CC.WHITE}%s
    """.trimIndent()

    @Coloured
    val cooldownDenyMessageAddition = "Purchase ${CC.GREEN}Plus${CC.RED} rank or higher to bypass this!"

    @Coloured
    var playerDataLoad =
        "${CC.RED}Your account did not load properly.\n${CC.RED}Please reconnect to resolve this issue."

    @Coloured
    val consoleName = "${CC.BD_RED}Console"

}
