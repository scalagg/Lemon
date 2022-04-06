package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.entity.Player
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 9/26/2021
 */
object PlaytimeCommand : BaseCommand() {

    @Syntax("<player>")
    @CommandAlias("playtime|pt")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.playtime")
    fun onPlayTime(
        player: Player, @Optional target: AsyncLemonPlayer?
    ): CompletableFuture<Void> {
        if (target != null && !player.hasPermission("lemon.command.playtime.other")) {
            throw ConditionFailedException("You do not have permission to view playtime of other players!")
        }

        if (target != null)
        {
            player.sendMessage("${CC.GREEN}Fetching playtime...")

            return target.validatePlayers(player, false) {
                handlePlaytimeComputation(player, it)
            }
        } else
        {
            return CompletableFuture
                .runAsync {
                    handlePlaytimeComputation(
                        player,
                        PlayerHandler.findPlayer(player)
                            .orElse(null)!!
                    )
                }
        }
    }

    private fun handlePlaytimeComputation(
        player: Player, target: LemonPlayer
    )
    {
        val zoneId = TimeZone.getDefault().toZoneId()
        val localDate = LocalDate.now(zoneId)

        val weekStart = localDate.with(
            TemporalAdjusters
                .previousOrSame(DayOfWeek.MONDAY)
        )
        val weekEnd = localDate.with(
            TemporalAdjusters
                .nextOrSame(DayOfWeek.SUNDAY)
        )

        val completePlaytime = target
            .pastLogins.values.sum()
        val completePlaytimeSessions = target
            .pastLogins.size

        val currentWeek = target
            .pastLogins.filter {
                val current = Instant.ofEpochMilli(it.value)
                    .atZone(zoneId)
                    .toLocalDate()

                return@filter current.isAfter(weekStart)
                        && current.isBefore(weekEnd)
            }
        val currentWeekPlaytime = currentWeek.values.sum()
        val currentWeekPlaytimeSessions = currentWeek.size

        val coloredName = QuickAccess.fetchColoredName(target.uniqueId)

        player.sendMessage("")
        player.sendMessage(" ${CC.B_SEC}Playtime of $coloredName${CC.B_SEC}...")
        player.sendMessage("  ${CC.SEC}This week ${CC.GRAY}(${currentWeekPlaytimeSessions} sessions)${CC.SEC}: ${CC.PRI}${
            DurationFormatUtils.formatDurationWords(
                currentWeekPlaytime, true, true
            )
        }")
        player.sendMessage("  ${CC.SEC}All-time total ${CC.GRAY}(${completePlaytimeSessions} sessions)${CC.SEC}: ${CC.PRI}${
            DurationFormatUtils.formatDurationWords(
                completePlaytime, true, true
            )
        }")
        player.sendMessage("")
    }
}
