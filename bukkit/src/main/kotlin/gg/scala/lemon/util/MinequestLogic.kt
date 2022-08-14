package gg.scala.lemon.util

import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 12/27/2021
 */
object MinequestLogic
{
    @JvmStatic
    val BLUE_CHARACTERS = listOf(
        "㽟", "㼰", "㼱", "㼲", "㼳", "㼴", "㼵", "㼶", "㼷", "㼸",
        "㼹", "㽁", "㽂", "㽃", "㽄", "㽅", "㽆", "㽇", "㽈", "㽉",
        "㽊", "㽋", "㽌", "㽍", "㽎", "㽏", "㽐", "㽑", "㽒", "㽓",
        "㽔", "㽕", "㽖", "㽗", "㽘", "㽙", "㽚", "㽡", "㽢", "㽣",
        "㽤", "㽥", "㽦", "㽧", "㽨", "㽩", "㽪", "㽫", "㽬", "㽭",
        "㽮", "㽯", "㽰", "㽱", "㽲", "㽳", "㽴", "㽵", "㽶", "㽷",
        "㽸", "㽹", "㽺"
    )

    @JvmStatic
    val RED_CHARACTERS = listOf(
        "｟", "Ｐ", "Ｑ", "Ｒ", "Ｓ", "Ｔ", "Ｕ", "Ｖ", "Ｗ", "Ｘ",
        "Ｙ", "ａ", "ｂ", "ｃ", "ｄ", "ｅ", "ｆ", "ｇ", "ｈ", "ｉ",
        "ｊ", "ｋ", "ｌ", "ｍ", "ｎ", "ｏ", "ｐ", "ｑ", "ｒ", "ｓ",
        "ｔ", "ｕ", "ｖ", "ｗ", "ｘ", "ｙ", "ｚ", "｡", "｢", "｣", "､",
        "･", "ｦ", "ｧ", "ｨ", "ｩ", "ｪ", "ｫ", "ｬ", "ｭ", "ｮ", "ｯ", "ｰ",
        "ｱ", "ｲ", "ｳ", "ｴ", "ｵ", "ｶ", "ｷ", "ｸ", "ｹ", "ｺ"
    )

    @JvmStatic
    val DEFAULT_CHARACTERS = listOf(
        "_", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B",
        "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b",
        "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
        "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    )

    @JvmStatic
    val CHARACTER_MAPPINGS = mutableMapOf(
        "blue" to BLUE_CHARACTERS,
        "red" to RED_CHARACTERS
    )

    @JvmStatic
    val RANK_MAPPINGS = mutableMapOf(
        listOf("owner", "mod", "srmod", "admin") to "red",
        listOf("platinum", "dev") to "blue"
    )

    @JvmStatic
    fun byRank(rank: Rank): String?
    {
        if (rank.name == "youtube")
        {
            return null
        }

        return RANK_MAPPINGS.entries
            .find {
                it.key.contains(rank.name.lowercase())
            }
            ?.value
    }

    @JvmStatic
    @JvmOverloads
    fun getTranslatedName(
        original: String, color: String = "blue"
    ): String
    {
        var translated = ""

        val colorMapping = CHARACTER_MAPPINGS[color]
            ?: return original

        for (i in original.indices)
        {
            translated += colorMapping[
                    DEFAULT_CHARACTERS.indexOf(
                        original[i].toString()
                    )
            ]
        }

        return CC.WHITE + translated
            .ifEmpty { original }
    }
}

