package gg.scala.lemon.command.moderation

import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants.AUTH_PREFIX
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.totp.TimeBasedOneTimePasswordUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 9/27/2021
 */
class AuthenticationCommand : BaseCommand() {

    @Syntax("[code]")
    @CommandPermission("lemon.2fa.forced")
    @CommandAlias("auth|2fa|authenticate")
    fun onAuth(player: Player, code: String) {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (!lemonPlayer.hasSetupAuthentication()) {
            throw ConditionFailedException("You have not setup authentication yet.")
        }

        if (lemonPlayer.isAuthExempt()) {
            throw ConditionFailedException("You're exempt from authentication.")
        }

        if (lemonPlayer.hasAuthenticatedThisSession()) {
            throw ConditionFailedException("You do not need to authenticate.")
        }

        val correctCode = TimeBasedOneTimePasswordUtil
            .generateCurrentNumberString(lemonPlayer.getAuthSecret())

        if (correctCode != code) {
            throw ConditionFailedException("The code you've provided is invalid.")
        }

        player.setMetadata(
            "authenticated",
            FixedMetadataValue(Lemon.instance, true)
        )

        lemonPlayer.savePreviousIpAddressAsCurrent = false

        lemonPlayer.removeMap()
        lemonPlayer.authenticateInternal()

        player.sendMessage("${AUTH_PREFIX}${CC.GREEN}You've been authenticated.")

//        BatUtil.makePlayerUnSitOnBat(player)
    }

    @Syntax("<target>")
    @CommandAlias("remove2fa")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.remove2fa")
    @Description("Remove & reset a specified player's 2fa.")
    fun onRemove2fa(sender: CommandSender, target: OnlinePlayer) {
        val lemonPlayer = PlayerHandler.findPlayer(target.player).orElse(null)

        if (!lemonPlayer.hasSetupAuthentication()) {
            throw ConditionFailedException("${CC.YELLOW}${target.player.name}${CC.RED} has not setup 2fa.")
        }

        lemonPlayer.removeMetadata("auth-secret")
        lemonPlayer.removeMetadata("auth-exempt")

        lemonPlayer.authenticateInternalReversed()

        sender.sendMessage("${CC.SEC}You've reset ${CC.PRI}${target.player.name}'s${CC.SEC} 2fa.")
    }

    @CommandAlias("setup2fa")
    @CommandPermission("lemon.2fa.forced")
    @Description("Setup two-factor-authentication for your account.")
    fun onSetup2fa(player: Player) {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (lemonPlayer.isAuthExempt()) {
            throw ConditionFailedException("You're exempt from authentication.")
        }

        if (lemonPlayer.hasSetupAuthentication()) {
            throw ConditionFailedException("You've already setup authentication, if you're looking to reset it, contact a manager.")
        }

        val base32Secret = TimeBasedOneTimePasswordUtil.generateBase32Secret()

        lemonPlayer.updateOrAddMetadata(
            "auth-secret",
            Metadata(base32Secret)
        )

        lemonPlayer.handleAuthMap(base32Secret)

        player.sendMessage(arrayOf(
            "${AUTH_PREFIX}A map with a QR code has been placed in your inventory.",
            "${AUTH_PREFIX}${CC.GRAY}Scan the QR code on any applicable authentication app and use the code to authenticate yourself using ${CC.WHITE}/auth <code>${CC.GRAY}."
        ))
    }
}
