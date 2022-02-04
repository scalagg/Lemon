package gg.scala.lemon.command

import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.util.data
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/9/2021
 */
class ChannelCommand : BaseCommand()
{
    @Private
    @CommandAlias("channel")
    @CommandPermission("lemon.command.channel")
    fun onDefault(player: Player, @Optional channel: Channel?)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (channel == null)
        {
            lemonPlayer.metadata["channel"]?.let {
                player.sendMessage("${CC.SEC}You're currently chatting in ${CC.PRI}${it.asString()}${CC.SEC}.")
            } ?: player.sendMessage("${CC.RED}You're chatting in the regular channel.")

            return
        }

        if (channel.getId() == "default")
        {
            throw ConditionFailedException("Use ${CC.YELLOW}/channel reset${CC.RED} to jump back to the regular channel.")
        }

        if (!channel.hasPermission(player))
        {
            throw ConditionFailedException("You do not have permission to chat in ${CC.YELLOW}${channel.getId()}${CC.RED}.")
        }

        lemonPlayer.updateOrAddMetadata(
            "channel",
            Metadata(channel.getId())
        )

        player.sendMessage("${CC.GREEN}You're now chatting in ${CC.YELLOW}${channel.getId().capitalize()}${CC.GREEN}.")
    }

    @Subcommand("list|showall")
    fun onShowAll(player: Player)
    {
        val viewable = ChatHandler.channels.values
            .filter { it.hasPermission(player) && it.getId() != "default" }
            .map { it.getId().capitalize() }

        if (viewable.isEmpty()) {
            throw ConditionFailedException("You do not have permission to chat in any other channel!")
        }

        player.sendMessage(
            "${CC.SEC}Channels: ${CC.PRI}${
                viewable.joinToString(
                    separator = "${CC.SEC}, ${CC.PRI}"
                )
            }${CC.SEC}."
        )
    }

    @Subcommand("reset")
    fun onReset(player: Player)
    {
        val lemonPlayer = (player data player.uniqueId)!!

        if (lemonPlayer doesNotHave "channel")
        {
            throw ConditionFailedException("You're talking in any channel.")
        }

        lemonPlayer remove "channel"
        lemonPlayer.save()

        player.sendMessage("${CC.GREEN}You've reset your channel.")
    }
}
