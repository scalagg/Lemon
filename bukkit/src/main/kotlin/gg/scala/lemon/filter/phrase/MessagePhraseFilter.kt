package gg.scala.lemon.filter.phrase

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
interface MessagePhraseFilter
{
    fun loadResources()
    fun isFiltered(player: Player, word: String): Boolean
}
