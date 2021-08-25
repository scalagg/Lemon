package com.solexgames.lemon

import com.google.gson.Gson
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.ChatColor
import net.evilblock.cubed.util.CC

object LemonConstants {

    @JvmStatic
    val STAFF_PREFIX: String = ""

    @JvmStatic
    val GSON: Gson = Serializers.gson

    @JvmStatic
    val SERVER_NAME: String = ""

    @JvmStatic
    val DISCORD_LINK: String = ""

    @JvmStatic
    val WEB_LINK: String = ""

    @JvmStatic
    val STORE_LINK: String = ""


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

    var MUTE_MESSAGE = ChatColor.RED.toString() + "You cannot speak as you are currently muted."
    var KICK_MESSAGE = ChatColor.RED.toString() + "You were kicked for: " + ChatColor.GRAY + "<reason>"

    @JvmStatic
    var SLOW_CHAT_MESSAGE = "${CC.RED}Chat is currently slowed, please wait <amount> before chatting again."
    var CMD_CHAT_MESSAGE = "${CC.RED}You're on command cooldown, please wait <amount>."
    var COOL_DOWN_MESSAGE = "${CC.RED}You're on chat cooldown, please wait <amount>."

    @JvmStatic
    var PLAYER_DATA_LOAD = """
        ${CC.RED}An error occurred while trying to load your data.
        ${CC.RED}Please try again later or contact a staff member.
        """.trimIndent()

    @JvmStatic
    var SERVER_NOT_LOADED = """
        ${ChatColor.RED}The server you've tried to connect to has not loaded.
        ${ChatColor.RED}Please try again in a few seconds or contact staff.
        """.trimIndent()

}
