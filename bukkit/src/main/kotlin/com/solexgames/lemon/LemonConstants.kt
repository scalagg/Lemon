package com.solexgames.lemon

import com.google.gson.reflect.TypeToken
import net.evilblock.cubed.acf.MessageKeys
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.Note
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object LemonConstants {

    @JvmStatic
    private val BASE_PREFIX = "${CC.PRI}${CC.BOLD}%s ${CC.GRAY}${CC.BOLD}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}"

    @JvmStatic
    val NULL: Optional<*> = Optional.ofNullable(null)

    @JvmStatic
    val STAFF = String.format(BASE_PREFIX, "Staff")

    @JvmStatic
    val LEMON = String.format(BASE_PREFIX, "Lemon")

    @JvmStatic
    val STRING_METADATA_MAP_TYPE = object : TypeToken<HashMap<String, Metadata>>() {}.type

    @JvmStatic
    val NOTE_ARRAY_LIST_TYPE = object : TypeToken<ArrayList<Note>>() {}.type

    @JvmStatic
    val STRING_ARRAY_LIST_TYPE = object : TypeToken<ArrayList<Note>>() {}.type

    @JvmStatic
    val GSON = Serializers.gson

    @JvmStatic
    val SERVER_NAME = Lemon.instance.lemonWebData.serverName

    @JvmStatic
    val DISCORD_LINK = Lemon.instance.lemonWebData.discord

    @JvmStatic
    val WEB_LINK = Lemon.instance.lemonWebData.domain

    @JvmStatic
    val STORE_LINK = Lemon.instance.lemonWebData.store

    @JvmStatic
    val NO_PERMISSION: String = MessageKeys.PERMISSION_DENIED.messageKey.key

    @JvmStatic
    val SINGLE_ROW = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)

    @JvmStatic
    val EMPTY = ItemBuilder(Material.STAINED_GLASS_PANE)
        .data(15)
        .name(" ")
        .toButton()

    @JvmStatic
    val FORMAT = SimpleDateFormat("MM/dd/yyyy HH:mma")

}
