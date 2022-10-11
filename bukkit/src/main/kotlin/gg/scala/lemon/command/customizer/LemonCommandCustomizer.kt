package gg.scala.lemon.customizer

import gg.scala.commons.acf.BukkitCommandCompletionContext
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.scala.lemon.Lemon
import gg.scala.lemon.channel.ChatChannel
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.stream.Collectors


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
                val parsed = Lemon.instance.parseUniqueIdFromContext(it)

                return@registerContext AsyncLemonPlayer
                    .of(
                        parsed.first, parsed.second
                    )
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
            val lemonPlayerOptional = PlayerHandler.findPlayer(firstArgument)

            if (!lemonPlayerOptional.isPresent)
            {
                throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
            }

            val lemonPlayer = lemonPlayerOptional.orElse(null)
                ?: throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")

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
                        .find(context.player.uniqueId)
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
