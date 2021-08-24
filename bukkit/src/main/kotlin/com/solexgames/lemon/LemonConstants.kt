package com.solexgames.lemon

import com.google.gson.Gson
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.ChatColor
import java.text.SimpleDateFormat

object LemonConstants {

    @JvmStatic
    val STAFF_PREFIX: String = ""

    @JvmStatic
    val GSON: Gson = Serializers.gson

    @JvmStatic
    val FORMAT: SimpleDateFormat = SimpleDateFormat("MM/dd/yyyy HH:mma")

    @JvmStatic
    val PRI: ChatColor = Lemon.instance.settings.primaryColor

    @JvmStatic
    val SEC: ChatColor = Lemon.instance.settings.secondaryColor

}
