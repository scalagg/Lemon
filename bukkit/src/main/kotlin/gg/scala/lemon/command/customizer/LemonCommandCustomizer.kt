package gg.scala.lemon.command.customizer

import gg.scala.commons.acf.BukkitCommandCompletionContext
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.InvalidCommandArgument
import gg.scala.commons.acf.MinecraftMessageKeys
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.scala.lemon.channel.ChatChannel
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.command.annotations.AllowOffline
import gg.scala.lemon.command.annotations.NoManualSelection
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.scope.ServerScope
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/14/2022
 */
object LemonCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(
        commandManager: ScalaCommandManager
    )
    {
        commandManager.commandCompletions
            .registerAsyncCompletion("permissions") { context ->
                val rank = context.getContextValue(Rank::class.java)
                    ?: return@registerAsyncCompletion listOf()

                val input = context.input.lowercase()

                rank.permissions
                    .filter { permission ->
                        input.isEmpty() || permission.startsWith(input)
                    }
            }

        commandManager.commandCompletions
            .registerAsyncCompletion("scopes") { context ->
                val rank = context.getContextValue(Rank::class.java)
                    ?: return@registerAsyncCompletion listOf()

                val input = context.input.lowercase()

                rank.scopes()
                    .filter { permission ->
                        input.isEmpty() || permission.group.startsWith(input)
                    }
                    .map(ServerScope::group)
            }

        commandManager.commandCompletions
            .registerAsyncCompletion("scopes:servers") { context ->
                val rank = context.getContextValue(ServerScope::class.java)
                    ?: return@registerAsyncCompletion listOf()

                val input = context.input.lowercase()

                rank.individual
                    .filter { server ->
                        input.isEmpty() || server.startsWith(input)
                    }
            }

        commandManager.commandCompletions
            .registerAsyncCompletion("ranks") {
                RankHandler.ranks.values.map(Rank::name)
            }

        commandManager.commandContexts
            .registerContext(Rank::class.java) {
                val firstArgument = it.popFirstArg()

                return@registerContext RankHandler.findRank(firstArgument)
                    ?: throw ConditionFailedException(
                        "No rank matching ${CC.YELLOW}$firstArgument${CC.RED} could be found."
                    )
            }

        commandManager.commandContexts
            .registerContext(AsyncLemonPlayer::class.java) {
                val noManualSelection = it.hasAnnotation(NoManualSelection::class.java)
                val allowOffline = it.hasAnnotation(AllowOffline::class.java)

                AsyncLemonPlayer.of(uniqueId = null, context = it)
                    .apply {
                        this.autoAccountSelectionOnMultiple = noManualSelection
                        this.allowOffline = allowOffline
                    }
            }

        commandManager.commandContexts
            .registerContext(ChatChannel::class.java) {
                val firstArgument = it.popFirstArg()

                return@registerContext ChatChannelService.find(firstArgument)
                    ?: throw ConditionFailedException(
                        "No channel matching ${CC.YELLOW}$firstArgument${CC.RED} could be found."
                    )
            }

        commandManager.commandContexts.registerContext(LemonPlayer::class.java) {
            val firstArgument = it.popFirstArg()
            val matches = Bukkit.getServer().matchPlayer(firstArgument)

            if (matches.size > 1)
            {
                val allMatches = matches
                    .joinToString(", ") { player ->
                        player.name
                    }

                throw InvalidCommandArgument(
                    MinecraftMessageKeys.MULTIPLE_PLAYERS_MATCH,
                    false,
                    "{search}", firstArgument,
                    "{all}", allMatches
                )
            }

            val lemonPlayerOptional = PlayerHandler.findPlayer(firstArgument)

            if (!lemonPlayerOptional.isPresent)
            {
                throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
            }

            val lemonPlayer = lemonPlayerOptional.get()

            if (it.player != null)
            {
                val selfLemonPlayer = PlayerHandler.find(it.player.uniqueId)
                    ?: return@registerContext lemonPlayer

                if (it.player.uniqueId == lemonPlayer.uniqueId)
                {
                    return@registerContext lemonPlayer
                }

                if (!selfLemonPlayer.canInteract(lemonPlayer))
                {
                    throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
                }
            }

            return@registerContext lemonPlayer
        }

        fun parseFromContext(context: BukkitCommandCompletionContext): List<String>
        {
            if (context.player == null)
            {
                return Bukkit.getOnlinePlayers().map(Player::getName)
            }

            val lemonPlayer = PlayerHandler
                .find(context.player.uniqueId)
                ?: return emptyList()

            return Bukkit.getOnlinePlayers()
                .filter {
                    val targetLemonPlayer = PlayerHandler
                        .find(it.uniqueId)
                        ?: return@filter false

                    lemonPlayer.canInteract(targetLemonPlayer)
                }
                .map(Player::getName)
        }

        commandManager.commandCompletions
            .registerAsyncCompletion(
                "players", ::parseFromContext
            )

        commandManager.commandCompletions
            .registerAsyncCompletion(
                "all-players", ::parseFromContext
            )
    }
}
