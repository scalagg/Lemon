package gg.scala.lemon

import com.google.gson.reflect.TypeToken
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.bukkit.Constants
import java.lang.reflect.Type
import java.util.*

object LemonConstants {

    // as CC.PRI & CC.SEC are instantiated later
    @JvmStatic
    val AUTH_PREFIX by lazy {
        "${CC.B_PRI}Staff ${CC.B_GRAY}${Constants.DOUBLE_ARROW_RIGHT} "
    }

    @JvmStatic
    val DEBUG by lazy {
        Reflection.DEBUG
    }

    @JvmStatic
    val FLAGS = mutableMapOf<String, (String) -> String>(
        "r" to {
            "${CC.D_GRAY}[${CC.D_RED}Alert${CC.D_GRAY}] ${CC.RESET}${Color.translate(it)}"
        }
    )

    @JvmStatic
    val STRING_METADATA_MAP_TYPE: Type = object : TypeToken<HashMap<String, Metadata>>() {}.type

    @JvmStatic
    val STRING_LONG_MUTABLE_MAP_TYPE: Type = object : TypeToken<MutableMap<String, Long>>() {}.type

    @JvmStatic
    val STRING_MUTABLE_LIST: Type = object : TypeToken<MutableList<String>>() {}.type

    @JvmStatic
    val UUID_MUTABLE_LIST: Type = object : TypeToken<MutableList<UUID>>() {}.type

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
    val NO_PERMISSION_SUB = "${CC.RED}You do not have permission to perform this subcommand!"

}
