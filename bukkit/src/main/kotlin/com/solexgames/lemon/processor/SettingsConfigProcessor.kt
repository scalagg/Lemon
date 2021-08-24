package com.solexgames.lemon.processor

import org.bukkit.ChatColor
import xyz.mkotb.configapi.comment.Comment

object SettingsConfigProcessor {

    @Comment("What should the server id for this instance be?")
    val id: String = "server-1"

    @Comment("What should the server group for this instance be?")
    val group: String = "hub"

    @Comment("What should the primary color be?")
    val primaryColor: ChatColor = ChatColor.AQUA

    @Comment("What should the secondary color be?")
    val secondaryColor: ChatColor = ChatColor.DARK_AQUA

}
