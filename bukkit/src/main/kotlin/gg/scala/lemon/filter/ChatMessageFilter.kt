package gg.scala.lemon.filter

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
interface ChatMessageFilter
{
    fun loadResources()

    fun formDescription(player: Player): String
    fun isFiltered(player: Player, message: String): Boolean
}
