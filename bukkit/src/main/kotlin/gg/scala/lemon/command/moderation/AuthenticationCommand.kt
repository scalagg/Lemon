package gg.scala.lemon.command.moderation

import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants.AUTH_PREFIX
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.totp.TimeBasedOneTimePasswordUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 9/27/2021
 */
class AuthenticationCommand : BaseCommand()
{
    @CommandPermission("lemon.2fa.forced")
    @CommandAlias("auth|2fa|authenticate")
    @CommandCompletion("@all-players")
    fun onAuth(player: Player, code: String)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (!lemonPlayer.hasSetupAuthentication())
        {
            throw ConditionFailedException("You have not setup authentication yet.")
        }

        if (lemonPlayer.isAuthExempt())
        {
            throw ConditionFailedException("You're exempt from authentication.")
        }

        if (lemonPlayer.hasAuthenticatedThisSession())
        {
            throw ConditionFailedException("You do not need to authenticate.")
        }

        val correctCode = TimeBasedOneTimePasswordUtil
            .generateCurrentNumberString(lemonPlayer.getAuthSecret())

        if (correctCode != code)
        {
            throw ConditionFailedException("The code you've provided is invalid.")
        }

        player.setMetadata(
            "authenticated",
            FixedMetadataValue(Lemon.instance, true)
        )

        lemonPlayer.savePreviousIpAddressAsCurrent = false

        lemonPlayer.removeMap()
        lemonPlayer.authenticateInternal()
        lemonPlayer.save()

        player.sendMessage("${AUTH_PREFIX}${CC.GREEN}You've been authenticated.")
    }

    @CommandAlias("2faexempt")
    @CommandPermission("op")
    @CommandCompletion("@all-players")
    @Description("Exempt a user from 2fa.")
    fun onExempt2fa(sender: CommandSender, target: LemonPlayer)
    {
        if (target doesNotHave "auth-exempt")
        {
            target.updateOrAddMetadata(
                "auth-exempt",
                Metadata(true)
            )

            target.authenticateInternal()
            target.save()

            sender.sendMessage("${AUTH_PREFIX}${CC.GREEN}They are now exempt.")
        } else
        {
            target remove "auth-exempt"
            target.authenticateInternalReversed()
            target.save()

            sender.sendMessage("${AUTH_PREFIX}${CC.GREEN}They are no longer exempt.")
        }
    }

    @Syntax("<target>")
    @CommandAlias("remove2fa")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.remove2fa")
    @Description("Remove & reset a specified player's 2fa.")
    fun onRemove2fa(sender: CommandSender, lemonPlayer: LemonPlayer)
    {
        if (!lemonPlayer.hasSetupAuthentication())
        {
            throw ConditionFailedException("${CC.YELLOW}${lemonPlayer.name}${CC.RED} has not setup 2fa.")
        }

        lemonPlayer remove "auth-secret"
        lemonPlayer remove "auth-exempt"

        lemonPlayer.authenticateInternalReversed()
        lemonPlayer.save()

        sender.sendMessage("${CC.SEC}You've reset ${CC.PRI}${lemonPlayer.name}'s${CC.SEC} 2fa.")
    }

    @CommandAlias("setup2fa")
    @CommandPermission("lemon.2fa.forced")
    @Description("Setup two-factor-authentication for your account.")
    fun onSetup2fa(player: Player)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (lemonPlayer.isAuthExempt())
        {
            throw ConditionFailedException("You're exempt from authentication.")
        }

        if (lemonPlayer.hasSetupAuthentication())
        {
            throw ConditionFailedException("You've already setup authentication, if you're looking to reset it, contact a manager.")
        }

        val base32Secret = TimeBasedOneTimePasswordUtil.generateBase32Secret()

        lemonPlayer.updateOrAddMetadata(
            "auth-secret",
            Metadata(base32Secret)
        )

        lemonPlayer.handleAuthMap(base32Secret)
        lemonPlayer.save()

        player.sendMessage(
            arrayOf(
                "$AUTH_PREFIX${CC.SEC}A map with a QR code has been placed in your inventory.",
                "$AUTH_PREFIX${CC.GRAY}Scan the QR code on any applicable authentication app and use the code to authenticate yourself using ${CC.WHITE}/auth <code>${CC.GRAY}."
            )
        )
    }
}
