package com.solexgames.lemon.command.conversation

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.metadata.Metadata
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.MessageKeys
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 29/08/2021 00:10
 */
class MessageCommand : BaseCommand() {

    @Syntax("<player> <message>")
    @CommandAlias("message|msg|tell|t|whisper|w")
    @CommandCompletion("@players-uv")
    fun onMessage(player: Player, target: OnlinePlayer, message: String) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
        val targetLemonPlayer = Lemon.instance.playerHandler.findPlayer(target.player).orElse(null)

        val pmSetting = lemonPlayer.getSetting("messages-disabled")
        val pmSettingTarget = targetLemonPlayer.getSetting("messages-disabled")

        if (!VisibilityHandler.treatAsOnline(target.player, player)) {
            player.sendMessage(MessageKeys.COULD_NOT_FIND_PLAYER.messageKey.key.replace("{search}", target.player.name))
        }
        if (pmSetting) {
            throw ConditionFailedException("You have private messages disabled.")
        }
        if (pmSettingTarget) {
            throw ConditionFailedException("${CC.YELLOW}${target.player.name} ${CC.RED}has private messages disabled.")
        }
        if (lemonPlayer.ignoring.contains(target.player.uniqueId)) {
            throw ConditionFailedException("You're ignoring this player.")
        }
        if (targetLemonPlayer.ignoring.contains(player.uniqueId)) {
            throw ConditionFailedException("That player is ignoring you.")
        }

        val soundSetting = targetLemonPlayer.getSetting("pm-sounds")

        if (soundSetting) {
            target.player.playSound(target.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
        }

        lemonPlayer.updateOrAddMetadata(
            "last-recipient",
            Metadata(target.player.uniqueId.toString())
        )

        targetLemonPlayer.updateOrAddMetadata(
            "last-recipient",
            Metadata(player.uniqueId.toString())
        )

        player.sendMessage("${CC.GRAY}(To ${targetLemonPlayer.getColoredName()}${CC.GRAY}) $message")
        target.player.sendMessage("${CC.GRAY}(From ${lemonPlayer.getColoredName()}${CC.GRAY}) $message")
    }
}
