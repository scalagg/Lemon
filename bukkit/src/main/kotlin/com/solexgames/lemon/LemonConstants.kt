package com.solexgames.lemon

import com.google.gson.Gson
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.ChatColor

object LemonConstants {

    @JvmStatic
    private val BASE_PREFIX: String = "${CC.PRI}${CC.BOLD}%s ${CC.GRAY}${CC.BOLD}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}"

    @JvmStatic
    val STAFF: String = String.format(BASE_PREFIX, "Staff")

    @JvmStatic
    val LEMON: String = String.format(BASE_PREFIX, "Lemon")

    @JvmStatic
    val GSON: Gson = Serializers.gson

    @JvmStatic
    val SERVER_NAME: String = Lemon.instance.lemonWebData.serverName

    @JvmStatic
    val DISCORD_LINK: String = Lemon.instance.lemonWebData.discord

    @JvmStatic
    val WEB_LINK: String = Lemon.instance.lemonWebData.domain

    @JvmStatic
    val STORE_LINK: String = Lemon.instance.lemonWebData.store

    val BAN_MESSAGE_TEMP = """&cYou are &4banned &cfrom $SERVER_NAME for <time>.
            &cYou were banned for: &7<reason> (ID: #<id>)
                &7If you feel this ban is unjustified, create a ticket at $WEB_LINK.
                &6You may also purchase an unban at $STORE_LINK."""
    val BAN_MESSAGE_PERM = """
        &cYou are permanently &4banned &cfrom $SERVER_NAME.
        &cYou were &4banned&c for: &7<reason> (ID: #<id>)
        &7If you feel this ban is unjustified, create a ticket at $WEB_LINK.
        &6You may also purchase an unban at $STORE_LINK.
        """.trimIndent()
    val BLACK_LIST_MESSAGE = """
        &cYou are blacklisted from $SERVER_NAME.&7
        &7You may not appeal this type of punishment.
        &4You may also not purchase an unban for this type of ban.
        """.trimIndent()
    val IP_BAN_RELATION_MESSAGE = """
        &cYour IP is permanently &4banned &cfrom $SERVER_NAME.
        &cYour ban is in relation to the account: &7<player>
        &7If you feel this ban is unjustified, create a ticket at $WEB_LINK.
        &6You can purchase an unban for <player> &6at $STORE_LINK.
        """.trimIndent()
    val BLACK_LIST_RELATION_MESSAGE = """
        &cYour IP is permanently &4blacklisted &cfrom $SERVER_NAME.
        &cYour blacklist is in relation to the account: &7<player>
        &7If you feel this ban is unjustified, create a ticket at $WEB_LINK.
        """.trimIndent()

    var MUTE_MESSAGE = "${CC.RED}You cannot speak as you are currently muted."
    var KICK_MESSAGE = "${CC.RED}You were kicked for: " + ChatColor.GRAY + "<reason>"

    @JvmStatic
    var PLAYER_DATA_LOAD = """
        ${CC.RED}An error occurred while trying to load your data.
        ${CC.RED}Please try again later or contact a staff member.
        """.trimIndent()

    @JvmStatic
    var SERVER_NOT_LOADED = "${CC.RED}The server has not loaded yet, please reconnect in a few seconds."

}
