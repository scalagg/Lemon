package com.solexgames.lemon.command.management.manual

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.QuickAccess.senderUuid
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.rank.Rank
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.time.Duration
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
class GrantManualCommand : BaseCommand() {

    @CommandAlias("grantmanual")
    @CommandPermission("lemon.command.grantmanual")
    @CommandCompletion("@all-players @ranks 1d|1w|1mo|3mo|6mo|1y|perm")
    fun onGrantManual(
        sender: CommandSender,
        target: UUID,
        rank: Rank,
        @Single duration: Duration,
        @Optional reason: String?
    ) {
        val grant = Grant(
            UUID.randomUUID(),
            target,
            rank.uuid,
            senderUuid(sender),
            System.currentTimeMillis(),
            Lemon.instance.settings.id,
            reason ?: "No reason provided",
            duration.get()
        )

        Lemon.instance.grantHandler.handleGrant(sender, grant)
    }

    @CommandAlias("grantmanualscope")
    @CommandPermission("lemon.command.grantmanualscope")
    @CommandCompletion("@all-players @ranks 1d|1w|1mo|3mo|6mo|1y|perm global")
    fun onGrantManualScope(
        sender: CommandSender,
        target: UUID,
        rank: Rank,
        @Single duration: Duration,
        @Single scopes: String,
        @Optional reason: String?
    ) {
        val splitScopes = scopes.split(",")
        val grant = Grant(
            UUID.randomUUID(),
            target,
            rank.uuid,
            senderUuid(sender),
            System.currentTimeMillis(),
            Lemon.instance.settings.id,
            reason ?: "No reason provided",
            duration.get()
        )

        grant.scopes.clear()

        splitScopes.forEach {
            if (!grant.scopes.contains(it)) {
                grant.scopes.add(it)
            }
        }

        Lemon.instance.grantHandler.handleGrant(sender, grant)
    }
}
