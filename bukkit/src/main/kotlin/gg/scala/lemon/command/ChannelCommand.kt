package gg.scala.lemon.command

import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Default
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.annotation.Subcommand
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/9/2021
 */
@CommandAlias("channel")
class ChannelCommand : BaseCommand() {

    @Default
    fun onDefault(player: Player, @Optional channel: Channel?) {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (channel == null) {
            lemonPlayer.metadata["channel"]?.let {
                player.sendMessage("${CC.SEC}You're currently chatting in ${CC.PRI}${it.asString()}${CC.SEC}.")
            } ?: player.sendMessage("${CC.RED}You're currently not chatting in a channel.")

            return
        }

        if (!channel.hasPermission(player)) {
            player.sendMessage("${CC.RED}You do not have permission to chat in ${CC.YELLOW}${channel.getId()}${CC.RED}.")
            return
        }

        lemonPlayer.updateOrAddMetadata(
            "channel",
            Metadata(channel.getId())
        )

        player.sendMessage("${CC.GREEN}You're now chatting in ${CC.YELLOW}${channel.getId()}${CC.GREEN}.")
    }

    @Subcommand("list|showall")
    fun onShowAll(player: Player) {
        player.sendMessage("${CC.SEC}Channels: ${CC.PRI}${
            ChatHandler.channels.values
                .map { it.getId() }
                .joinToString(
                    separator = "${CC.YELLOW}, ${CC.PRI}"
                )
        }${CC.SEC}.")
    }

    @Subcommand("reset")
    fun onReset(player: Player) {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)
        lemonPlayer.removeMetadata("channel")

        player.sendMessage("${CC.GREEN}You've reset your channel.")
    }
}
