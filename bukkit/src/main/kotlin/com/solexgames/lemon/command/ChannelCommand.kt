package com.solexgames.lemon.command

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.player.metadata.Metadata
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
@CommandAlias("channel|chat")
class ChannelCommand : BaseCommand() {

    @Default
    fun onDefault(player: Player, @Optional channel: Channel?) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

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
            Lemon.instance.chatHandler.channels.values
                .map { it.getId() }
                .joinToString(
                    separator = "${CC.YELLOW}, ${CC.PRI}"
                )
        }${CC.SEC}.")
    }

    @Subcommand("reset")
    fun onReset(player: Player) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
        lemonPlayer.removeMetadata("channel")

        player.sendMessage("${CC.GREEN}You've reset your channel.")
    }
}
