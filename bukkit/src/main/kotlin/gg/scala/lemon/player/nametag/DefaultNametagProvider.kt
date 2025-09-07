package gg.scala.lemon.player.nametag

import gg.scala.game.extensions.guilds.Guild
import gg.scala.game.extensions.guilds.GuildService
import gg.scala.game.extensions.profile.CorePlayerProfileService
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.internal.ExtHookIns
import gg.scala.lemon.minequest
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.sorter.SortedRankCache
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.realRank
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object DefaultNametagProvider : NametagProvider("default", 1) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo? {
        val lemonPlayer = PlayerHandler.find(toRefresh.uniqueId) ?: return null

        val rank = lemonPlayer.disguiseRank()
            ?: QuickAccess.computeRank(toRefresh.uniqueId).join()!!

        val sortMapping = SortedRankCache.teamMappings[rank.uuid]
        val prefix = buildPrefix(rank)

        val guild = GuildService.guildByUser(toRefresh.uniqueId).join()

        val suffix = buildGuildTag(guild)

        val teamSorting = if (Lemon.instance.settings.tablistSortingEnabled) {
            (sortMapping ?: "z").take(16)
        } else {
            "§z§z§z"
        }

        return createNametag(
            prefix.take(16),
            suffix.take(16),
            teamSorting.take(16)
        )
    }

    private fun buildPrefix(rank: Rank): String {
        return if (Lemon.instance.settings.prefixInNametags) {
            QuickAccess.replaceEmptyNullable(rank.prefix)
                ?.let { "$it${rank.color} " }
                ?: rank.color
        } else {
            rank.color
        }.take(16)
    }

    private fun buildGuildTag(guild: Guild?): String {
        if (!Lemon.instance.settings.guildTagsInNametags) {
            return ""
        }

        val tag = guild?.tag

        return if (tag == null) {
            ""
        } else {
            " ${CC.GRAY}[$tag]${CC.RESET}".take(16)
        }
    }
}