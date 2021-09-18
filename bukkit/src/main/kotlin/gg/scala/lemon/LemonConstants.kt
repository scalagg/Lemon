package gg.scala.lemon

import com.cryptomorin.xseries.XMaterial
import com.google.gson.reflect.TypeToken
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.acf.MessageKeys
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.Note
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object LemonConstants {

    @JvmStatic
    private val BASE_PREFIX = "${CC.PRI}${CC.BOLD}%s ${CC.GRAY}${CC.BOLD}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}"

    @JvmStatic
    val STAFF = String.format(BASE_PREFIX, "Staff")

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
    val DEV = Lemon.instance.lemonWebData.serverName == "SolexGames"

    @JvmStatic
    val NO_PERMISSION_SUB: String = "${CC.RED}You do not have permission to perform this subcommand!"

    @JvmStatic
    val EMPTY = ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE)
        .data(15)
        .name(" ")
        .toButton()

}
