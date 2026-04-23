package gg.scala.lemon.command.management.manual

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.QuickAccess.senderUuid
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.acf.annotation.Optional
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import net.evilblock.cubed.util.time.Duration
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
@AutoRegister
object GrantManualCommand : ScalaCommand()
{
    @CommandAlias("grantmanual")
    @CommandPermission("lemon.command.grantmanual")
    @CommandCompletion("@players @ranks 1d|1w|1mo|3mo|6mo|1y|perm|permanent")
    fun onGrantManual(
        sender: CommandSender,
        target: AsyncLemonPlayer,
        rank: Rank,
        @Single duration: Duration,
        @Optional reason: String?
    ): CompletableFuture<Void>
    {
        return target.validatePlayers(sender, true) {
            val grant = Grant(
                UUID.randomUUID(),
                it.uniqueId,
                rank.uuid,
                senderUuid(sender),
                System.currentTimeMillis(),
                Lemon.instance.settings.id,
                reason ?: "No reason provided",
                duration.get()
            )

            GrantHandler.handleGrant(sender, grant)
        }
    }

    @CommandAlias("grantmanualscope")
    @CommandPermission("lemon.command.grantmanualscope")
    @CommandCompletion("@players @ranks 1d|1w|1mo|3mo|6mo|1y|perm|permanent @scopes")
    fun onGrantManualScope(
        sender: CommandSender,
        target: AsyncLemonPlayer,
        rank: Rank,
        @Single duration: Duration,
        @Single scopes: String,
        @Optional reason: String?
    ): CompletableFuture<Void>
    {
        return target.validatePlayers(sender, true) {
            val splitScopes = scopes
                .split(",")

            val grant = Grant(
                UUID.randomUUID(),
                it.uniqueId,
                rank.uuid,
                senderUuid(sender),
                System.currentTimeMillis(),
                Lemon.instance.settings.id,
                reason ?: "No reason provided",
                duration.get()
            )

            grant.scopes.clear()

            splitScopes.forEach {
                if (!grant.scopes.contains(it))
                {
                    grant.scopes.add(it)
                }
            }

            GrantHandler.handleGrant(sender, grant)
        }
    }
}
