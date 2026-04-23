package gg.scala.lemon.filter.phrase.impl

import gg.scala.lemon.filter.phrase.MessagePhraseFilter
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
object MinequestInvalidCharFilter : MessagePhraseFilter
{
    private val validCharacters =
        mutableListOf<Char>()

    override fun loadResources()
    {
        val mappings = """
            áÁàÀâÂäÄãÃåÅæÆçÇéÉèÈêÊëËíÍìÌîÎïÏñÑóÓòÒôÔöÖõÕøØœŒßúÚùÙûÛüÜ
        """.trimIndent()

        validCharacters += 'A'..'Z'
        validCharacters += 'a'..'z'
        validCharacters += '1'..'9'
        validCharacters += listOf(
            '!', '@', '#', '$', '%',
            '^', '&', '*', '(', ')',
            '-', '_', '+', '[', ']', '{', '}',
            '}', '|', '\\', ';', ':',
            '"', '\'', ',', '<', '>',
            '.', '/', '?', '~', '=',
            '\n', '\t', '\r', '`'
        )

        for (mapping in mappings)
        {
            validCharacters += mapping
        }
    }

    override fun isFiltered(
        player: Player, word: String
    ): Boolean
    {
        val characters = word
            .toCharArray()

        return characters.any {
            !this.validCharacters.contains(it)
        }
    }
}
