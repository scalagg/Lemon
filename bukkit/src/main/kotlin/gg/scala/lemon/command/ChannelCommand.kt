package gg.scala.lemon.command

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.channel.ChatChannel
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.util.data
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/9/2021
 */
@AutoRegister
@CommandAlias("channel")
object ChannelCommand : ScalaCommand()
{
    @Private
    @CommandPermission("lemon.command.channel")
    fun onDefault(player: Player, @Optional channel: ChatChannel?)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (channel == null)
        {
            lemonPlayer.metadata["channel"]?.let {
                player.sendMessage("${CC.SEC}You're currently chatting in ${CC.PRI}${it.asString()}${CC.SEC}.")
            } ?: player.sendMessage("${CC.RED}You're chatting in the regular channel.")

            return
        }

        if (channel.composite().identifier() == "default")
        {
            throw ConditionFailedException("Use ${CC.YELLOW}/channel reset${CC.RED} to jump back to the regular channel.")
        }

        if (!channel.permissionLambda.invoke(player))
        {
            throw ConditionFailedException("You do not have permission to chat in ${CC.YELLOW}${channel.composite().identifier()}${CC.RED}.")
        }

        lemonPlayer.updateOrAddMetadata(
            "channel",
            Metadata(
                channel.composite().identifier()
            )
        )

        player.sendMessage("${CC.GREEN}You're now chatting in ${CC.YELLOW}${
            channel.composite().identifier().capitalize()
        }${CC.GREEN}.")
    }

    @Subcommand("list|showall")
    fun onShowAll(player: Player)
    {
        val viewable = ChatChannelService
            .accessibleChannels(player)

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
