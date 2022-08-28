package gg.scala.lemon.internal

import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.rank.Rank

/**
 * @author GrowlyX
 * @since 8/28/2022
 */
object ExtHookIns
{
    var customColorMappingChatColor: (String, LemonPlayer) -> String = { _, _ -> "" }
    var customColorMappingFormatted: (String, LemonPlayer) -> String = { _, _ -> "" }

    var customRankColoredName: (Rank) -> String? = { null }
    var customPlayerColoredName: (LemonPlayer, Rank, Boolean, Boolean) -> String? = { _, _, _, _ -> null }

    var customPlayerColoredNameOriginal: (LemonPlayer, Rank, Boolean) -> String? = { _, _, _ -> null }
}
