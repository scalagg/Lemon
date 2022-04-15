package gg.scala.lemon.customizer

import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.lemon.Lemon
import gg.scala.lemon.channel.ChatChannel
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 4/14/2022
 */
object LemonCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(
        commandManager: CubedCommandManager
    )
    {
        commandManager.commandCompletions.registerAsyncCompletion("ranks") {
            return@registerAsyncCompletion RankHandler.ranks.map { it.value.name }
        }

        commandManager.commandContexts.registerContext(Rank::class.java) {
            val firstArgument = it.popFirstArg()

            return@registerContext RankHandler.findRank(firstArgument)
                ?: throw ConditionFailedException("No rank matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
        }

        commandManager.commandContexts
            .registerContext(AsyncLemonPlayer::class.java) {
                val parsed = Lemon.instance.parseUniqueIdFromContext(it)

                return@registerContext AsyncLemonPlayer.of(
                    parsed.first, parsed.second
                )
            }

        commandManager.commandContexts.registerContext(ChatChannel::class.java) {
            val firstArgument = it.popFirstArg()

            return@registerContext ChatChannelService.find(firstArgument)
                ?: throw ConditionFailedException("No channel matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
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
                if (!VisibilityHandler.treatAsOnline(lemonPlayer.bukkitPlayer!!, it.player))
                {
                    throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
                }
            }

            return@registerContext lemonPlayer
        }

        commandManager.commandCompletions.registerAsyncCompletion("all-players") {
            return@registerAsyncCompletion mutableListOf<String>().also {
                Bukkit.getOnlinePlayers()
                    .filter { !it.hasMetadata("vanished") }
                    .forEach { player ->
                        it.add(player.name)
                    }
            }
        }

        commandManager.commandCompletions.registerAsyncCompletion("players") {
            return@registerAsyncCompletion mutableListOf<String>().also {
                Bukkit.getOnlinePlayers()
                    .filter { !it.hasMetadata("vanished") }
                    .forEach { player -> it.add(player.name) }
            }
        }
    }
}
