package gg.scala.lemon.filter.phrase

import org.bukkit.entity.Player

interface MessagePhraseFilter
{
    fun loadResources()
    fun isFiltered(player: Player, word: String): Boolean
}
