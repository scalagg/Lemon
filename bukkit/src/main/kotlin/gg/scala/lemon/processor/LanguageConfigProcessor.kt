package gg.scala.lemon.processor

import gg.scala.lemon.LemonConstants
import net.evilblock.cubed.util.CC
import xyz.mkotb.configapi.Coloured

/**
 * @author GrowlyX
 * @since 8/28/2021
 */
class LanguageConfigProcessor {

    @Coloured
    val tempBanMessage =
        "${CC.RED}You are banned from ${LemonConstants.SERVER_NAME} for %s.\n" +
                "${CC.RED}You were banned for: ${CC.GRAY}%s (#%s)\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}.\n" +
                "${CC.GOLD}You may also purchase an unban at ${LemonConstants.STORE_LINK}${CC.GOLD}."

    @Coloured
    val permBanMessage =
        "${CC.RED}You are permanently banned from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.RED}You were banned for: ${CC.GRAY}%s (#%s)\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}.\n" +
                "${CC.GOLD}You may also purchase an unban at ${LemonConstants.STORE_LINK}${CC.GOLD}."

    @Coloured
    val blacklistMessage =
        "${CC.RED}You are blacklisted from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.GRAY}You may not appeal this type of punishment.\n" +
                "${CC.GOLD}You may also not purchase an unban for this type of ban."

    @Coloured
    val banRelationTemporaryMessage =
        "${CC.RED}Your IP is temporarily banned from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.RED}Your ban is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}.\n" +
                "${CC.GOLD}You may also purchase an unban for %s${CC.GOLD} at ${LemonConstants.STORE_LINK}."

    @Coloured
    val banRelationPermanentMessage =
        "${CC.RED}Your IP is permanently banned from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.RED}Your ban is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}.\n" +
                "${CC.GOLD}You may also purchase an unban for %s${CC.GOLD} at ${LemonConstants.STORE_LINK}."

    @Coloured
    val blacklistRelationMessage =
        "${CC.RED}Your IP is permanently blacklisted from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.RED}Your blacklist is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}."

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
    var playerDataLoad = "${CC.RED}Your account did not load properly.\n${CC.RED}Please reconnect to resolve this issue."

    @Coloured
    val consoleName = "${CC.BD_RED}Console"

}
