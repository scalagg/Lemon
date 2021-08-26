package com.solexgames.lemon.player.channel

import com.solexgames.lemon.type.Permissible
import com.solexgames.lemon.type.Prefixable
import com.solexgames.lemon.player.rank.Rank
import org.bukkit.entity.Player

interface Channel: Prefixable, Permissible<Player> {

    fun getId(): String

    fun isGlobal(): Boolean

    fun getFormatted(message: String, sender: String, rank: Rank, receiver: Player): String

    override fun isPrefixed(message: String): Boolean {
        return message.startsWith("${getPrefix()} ")
    }

}
