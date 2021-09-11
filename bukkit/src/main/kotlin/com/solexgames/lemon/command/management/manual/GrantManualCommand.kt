package com.solexgames.lemon.command.management.manual

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.lemon.util.quickaccess.parseDuration
import com.solexgames.lemon.util.quickaccess.senderUuid
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.time.DateUtil
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
class GrantManualCommand : BaseCommand() {

    @CommandAlias("grantmanual")
    @Syntax("<player> <rank> <duration> [reason]")
    @CommandPermission("lemon.command.grantmanual")
    @CommandCompletion("@players @ranks 1d|1w|1mo|3mo|6mo|1y|perm")
    fun onGrantManual(
        sender: CommandSender,
        target: UUID,
        rank: Rank,
        duration: String,
        reason: String
    ) {
        val actualDuration = parseDuration(duration)
        val grant = Grant(
            UUID.randomUUID(),
            target,
            rank.uuid,
            senderUuid(sender),
            System.currentTimeMillis(),
            Lemon.instance.settings.id,
            reason,
            actualDuration
        )

        Lemon.instance.grantHandler.handleGrant(sender, grant)
    }

    @CommandAlias("grantmanualscope")
    @Syntax("<player> <rank> <duration> <scopes> [reason]")
    @CommandPermission("lemon.command.grantmanualscope")
    @CommandCompletion("@players @ranks 1d|1w|1mo|3mo|6mo|1y|perm global")
    fun onGrantManualScope(
        sender: CommandSender,
        target: UUID,
        rank: Rank,
        duration: String,
        scope: String,
        reason: String
    ) {
        val actualDuration = parseDuration(duration)
        val actualScopes = scope.split(",")
        val grant = Grant(
            UUID.randomUUID(),
            target,
            rank.uuid,
            senderUuid(sender),
            System.currentTimeMillis(),
            Lemon.instance.settings.id,
            reason,
            actualDuration
        )

        actualScopes.forEach {
            if (!grant.scopes.contains(it)) {
                grant.scopes.add(it)
            }
        }

        Lemon.instance.grantHandler.handleGrant(sender, grant)
    }
}
