package gg.scala.lemon.filter

import org.bukkit.entity.Player

interface ChatMessageFilter
{
    fun loadResources()

    fun formDescription(player: Player): String
    fun isFiltered(player: Player, message: String): Boolean
}
