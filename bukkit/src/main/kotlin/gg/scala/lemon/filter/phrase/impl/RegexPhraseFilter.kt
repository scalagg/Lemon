package gg.scala.lemon.filter.phrase.impl

import gg.scala.lemon.Lemon
import gg.scala.lemon.filter.phrase.MessagePhraseFilter
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
object RegexPhraseFilter : MessagePhraseFilter
{
    private val regexes = mutableListOf<Regex>()

    override fun loadResources()
    {
        regexes.addAll(
            Lemon.instance.settings
                .blacklistedPhraseRegex
                .map { it.toRegex() }
        )
    }

    override fun isFiltered(
        player: Player, word: String
    ): Boolean
    {
        return regexes.any {
            it.matches(word)
        }
    }
}
