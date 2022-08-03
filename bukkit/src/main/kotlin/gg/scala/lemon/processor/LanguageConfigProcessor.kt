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
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg.\n" +
                "${CC.GOLD}You may also purchase an unban at store.scala.gg${CC.GOLD}."

    @Coloured
    val permBanMessage =
        "${CC.RED}You are permanently banned from Scala\n" +
                "${CC.RED}You were banned for: ${CC.GRAY}%s (#%s)\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg.\n" +
                "${CC.GOLD}You may also purchase an unban at store.scala.gg${CC.GOLD}."

    @Coloured
    val blacklistMessage =
        "${CC.RED}You are blacklisted from Scala\n" +
                "${CC.GRAY}You may not appeal this type of punishment.\n" +
                "${CC.GOLD}You may also not purchase an unban for this type of ban."

    @Coloured
    val banRelationTemporaryMessage =
        "${CC.RED}Your IP is temporarily banned from Scala\n" +
                "${CC.RED}Your ban is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg.\n" +
                "${CC.GOLD}You may also purchase an unban for %s${CC.GOLD} at store.scala.gg."

    @Coloured
    val banRelationPermanentMessage =
        "${CC.RED}Your IP is permanently banned from Scala\n" +
                "${CC.RED}Your ban is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg.\n" +
                "${CC.GOLD}You may also purchase an unban for %s${CC.GOLD} at store.scala.gg."

    @Coloured
    val blacklistRelationMessage =
        "${CC.RED}Your IP is permanently blacklisted from Scala\n" +
                "${CC.RED}Your blacklist is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at www.scala.gg."

    @Coloured
    var frozenPlayerHasTimeMessage = """
        ${CC.RED}  
        ${CC.RED}You've been frozen by a staff member!
        ${CC.YELLOW}You have ${CC.WHITE}%s${CC.YELLOW} to join ${CC.GREEN}discord.scala.gg${CC.YELLOW}.
        ${CC.GRAY}If you fail to comply with our staff team's orders, you will be banned.
        ${CC.RED}  
    """.trimIndent()

    @Coloured
    var frozenPlayerTimeIsUpMessage = """
        ${CC.RED}  
        ${CC.RED}You've been frozen by a staff member!
        ${CC.YELLOW}It has been 5 minutes! Please join as soon as possible.
        ${CC.GRAY}If you fail to comply with our staff team's orders, you will be banned.
        ${CC.RED}  
    """.trimIndent()

    @Coloured
    @Comment(
        "The replacements for this mute message are as follows:",
        " - Tense: \"You've been/You're currently\"",
        " - Reason",
        " - Expiration: \"not expire/expire in <date>\"",
    )
    val muteMessage = """
        ${CC.RED}%s muted for: ${CC.WHITE}%s
        ${CC.RED}This punishment will %s.
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
    var playerDataLoad =
        "${CC.RED}Your account did not load properly.\n${CC.RED}Please reconnect to resolve this issue."

    @Coloured
    val consoleName = "${CC.BD_RED}Console"

}
