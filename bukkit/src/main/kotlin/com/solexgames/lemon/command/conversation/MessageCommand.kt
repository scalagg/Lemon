package com.solexgames.lemon.command.conversation

import com.cryptomorin.xseries.XSound
import com.solexgames.lemon.Lemon
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author puugz
 * @since 29/08/2021 00:10
 */
class MessageCommand: BaseCommand() {

    @Syntax("<player> <message>")
    @CommandAlias("message|msg|tell|t|whisper|w")
    fun onMessage(player: Player, target: OnlinePlayer, message: String) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
        val targetLemonPlayer = Lemon.instance.playerHandler.findPlayer(target.player).orElse(null)

        val pmSetting = lemonPlayer.getSetting("private-messages")
        val pmSettingTarget = targetLemonPlayer.getSetting("private-messages")

        if (!VisibilityHandler.treatAsOnline(target.player, player)) {
            throw ConditionFailedException("Could not find that player.")
        }
        if (!pmSetting) {
            throw ConditionFailedException("You have private messages disabled.")
        }
        if (!pmSettingTarget) {
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
            target.player.playSound(target.player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 5f)
        }

        lemonPlayer.lastRecipient = target.player.name
        targetLemonPlayer.lastRecipient = player.name

        player.sendMessage("${CC.GRAY}(To ${targetLemonPlayer.getColoredName()}${CC.GRAY}) $message")
        target.player.sendMessage("${CC.GRAY}(From ${lemonPlayer.getColoredName()}${CC.GRAY}) $message")
    }
}
