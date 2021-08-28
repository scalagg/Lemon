package com.solexgames.lemon

import com.google.gson.Gson
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material
import java.text.SimpleDateFormat

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

    @JvmStatic
    val SINGLE_ROW =  listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)

    @JvmStatic
    val EMPTY = ItemBuilder(Material.STAINED_GLASS_PANE)
        .data(15)
        .name(" ")
        .toButton()

    @JvmStatic
    val FORMAT = SimpleDateFormat("MM/dd/yyyy HH:mma")

}
