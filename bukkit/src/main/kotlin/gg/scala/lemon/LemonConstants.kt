package gg.scala.lemon

import com.google.gson.reflect.TypeToken
import gg.scala.lemon.metadata.NetworkMetadataDataSync
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.bukkit.Constants
import java.lang.reflect.Type
import java.util.*

object LemonConstants {

    @JvmStatic
    val DEBUG by lazy {
        Reflection.DEBUG
    }

    @JvmStatic
    val STRING_METADATA_MAP_TYPE: Type = object : TypeToken<HashMap<String, Metadata>>() {}.type

    @JvmStatic
    val STRING_LONG_MUTABLE_MAP_TYPE: Type = object : TypeToken<MutableMap<String, Long>>() {}.type

    @JvmStatic
    val STRING_MUTABLE_LIST: Type = object : TypeToken<MutableList<String>>() {}.type

    @JvmStatic
    val STRING_MUTABLE_SET: Type = object : TypeToken<MutableSet<String>>() {}.type

    @JvmStatic
    val UUID_MUTABLE_LIST: Type = object : TypeToken<MutableList<UUID>>() {}.type

    @JvmStatic
    val SERVER_NAME: String
        get() = NetworkMetadataDataSync.serverName()

    @JvmStatic
    val DISCORD_LINK: String
        get() = NetworkMetadataDataSync.cached().discord

    @JvmStatic
    val WEB_LINK: String
        get() = NetworkMetadataDataSync.cached().domain

    @JvmStatic
    val STORE_LINK: String
        get() = NetworkMetadataDataSync.cached().store

    @JvmStatic
    val CONSOLE: String
        get() {
            return Lemon.instance.languageConfig.consoleName
        }

    @JvmStatic
    val LOBBY: Boolean
        get() {
            return Lemon.instance.settings.group.contains("hub", true)
        }

    @JvmStatic
    val NO_PERMISSION_SUB = "${CC.RED}You do not have permission to perform this subcommand!"

}
