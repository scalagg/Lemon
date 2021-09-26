package gg.scala.lemon

import com.google.gson.reflect.TypeToken
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.Note
import java.lang.reflect.Type
import java.util.*

object LemonConstants {

    // as CC.PRI & CC.SEC are instantiated later
    @JvmStatic
    val AUTH_PREFIX: String
        get() {
            return "${CC.PRI}${CC.BOLD}2FA ${CC.GRAY}${CC.BOLD}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}"
        }

    @JvmStatic
    val STRING_METADATA_MAP_TYPE: Type = object : TypeToken<HashMap<String, Metadata>>() {}.type

    @JvmStatic
    val STRING_LONG_MUTABLEMAP_TYPE: Type = object : TypeToken<MutableMap<String, Long>>() {}.type

    @JvmStatic
    val NOTE_ARRAY_LIST_TYPE: Type = object : TypeToken<ArrayList<Note>>() {}.type

    @JvmStatic
    val UUID_ARRAY_LIST_TYPE: Type = object : TypeToken<MutableList<UUID>>() {}.type

    @JvmStatic
    val SERVER_NAME = Lemon.instance.lemonWebData.serverName

    @JvmStatic
    val DISCORD_LINK = Lemon.instance.lemonWebData.discord

    @JvmStatic
    val WEB_LINK = Lemon.instance.lemonWebData.domain

    @JvmStatic
    val STORE_LINK = Lemon.instance.lemonWebData.store

    @JvmStatic
    val CONSOLE: String
        get() {
            return Lemon.instance.languageConfig.consoleName
        }

    @JvmStatic
    val LOBBY: Boolean
        get() {
            return Lemon.instance.settings.group.equals("hub", true)
        }

    @JvmStatic
    val NO_PERMISSION_SUB: String = "${CC.RED}You do not have permission to perform this subcommand!"

}
