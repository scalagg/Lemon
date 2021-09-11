package com.solexgames.lemon.processor

import net.evilblock.cubed.util.CC
import com.solexgames.lemon.LemonConstants
import xyz.mkotb.configapi.Coloured

/**
 * @author GrowlyX
 * @since 8/28/2021
 */
class LanguageConfigProcessor {

    @Coloured
    val tempBanMessage =
        "${CC.RED}You are ${CC.DARK_RED}banned${CC.RED} from ${LemonConstants.SERVER_NAME} for %s.\n" +
                "${CC.RED}You were banned for: ${CC.GRAY}%s (#%s)\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}.\n" +
                "${CC.GOLD}You may also purchase an unban at ${LemonConstants.STORE_LINK}"

    @Coloured
    val permBanMessage =
        "${CC.RED}You are permanently ${CC.DARK_RED}banned${CC.RED} from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.RED}You were banned for: ${CC.GRAY}%s (#%s)\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}.\n" +
                "${CC.GOLD}You may also purchase an unban at ${LemonConstants.STORE_LINK}"

    @Coloured
    val blacklistMessage =
        "${CC.RED}You are permanently ${CC.DARK_RED}blacklisted${CC.RED} from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.GRAY}You may not appeal this type of punishment.\n" +
                "${CC.GOLD}You may also not purchase an unban for this type of ban."

    @Coloured
    val banRelationMessage =
        "${CC.RED}Your IP is temporarily ${CC.DARK_RED}banned${CC.RED} from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.RED}Your ban is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}.\n" +
                "${CC.GOLD}You may also purchase an unban for %s at ${LemonConstants.STORE_LINK}"

    @Coloured
    val blacklistRelationMessage =
        "${CC.RED}Your IP is permanently ${CC.DARK_RED}blacklisted${CC.RED} from ${LemonConstants.SERVER_NAME}\n" +
                "${CC.RED}Your blacklist is in relation to the account: ${CC.GRAY}%s\n" +
                "${CC.GRAY}If you feel this ban is unjustified, create a ticket at ${LemonConstants.DISCORD_LINK}."

    @Coloured
    var playerDataLoad = "${CC.RED}Your account did not load properly.\n${CC.GRAY}Please reconnect to resolve this issue."

    @Coloured
    var serverNotLoaded = "${CC.RED}The server is still loading.\n${CC.GRAY}Please reconnect in a few seconds."

}
