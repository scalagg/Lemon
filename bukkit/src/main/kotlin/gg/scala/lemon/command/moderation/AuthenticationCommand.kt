package gg.scala.lemon.command.moderation

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.LemonConstants.AUTH_PREFIX
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.util.BatUtil
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Description
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.totp.TimeBasedOneTimePasswordUtil
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

        player.inventory.remove(XMaterial.MAP.parseMaterial())

        BatUtil.makePlayerUnSitOnBat(player)
    }

    @CommandAlias("setup2fa")
    @CommandPermission("lemon.2fa.forced")
    @Description("Setup two-factor-authentication for your account.")
    fun onSetup2fa(player: Player) {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (lemonPlayer.isAuthExempt()) {
            throw ConditionFailedException("You're exempt from authentication.")
        }

        if (!lemonPlayer.hasSetupAuthentication()) {
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