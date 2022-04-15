package gg.scala.lemon.command.moderation

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.frozen.FrozenPlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.event.impl.PostFreezeEvent
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.nameOrConsole
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
@AutoRegister
object FreezeCommand : ScalaCommand()
{
    @Syntax("<player>")
    @CommandAlias("freeze|fr|ss")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.freeze")
    fun onFreeze(sender: CommandSender, target: LemonPlayer)
    {
        val alreadyFrozen = target.bukkitPlayer!!.hasMetadata("frozen")

        if (alreadyFrozen)
        {
            sendStaffMessage(
                "${CC.AQUA}${nameOrConsole(sender)}${CC.D_AQUA} has unfrozen ${coloredName(target.bukkitPlayer!!)}${CC.D_AQUA}.",
                true
            )

            sender.sendMessage("${CC.GREEN}You've unfrozen ${coloredName(target.bukkitPlayer!!)}${CC.GREEN}.")
            target.bukkitPlayer!!.removeMetadata("frozen", Lemon.instance)

            FrozenPlayerHandler.expirables.remove(target.bukkitPlayer!!.uniqueId)
        } else
        {
            sendStaffMessage(
                "${CC.AQUA}${nameOrConsole(sender)}${CC.D_AQUA} has frozen ${coloredName(target.bukkitPlayer!!)}${CC.D_AQUA}.",
                true
            )

            sender.sendMessage("${CC.GREEN}You've frozen ${coloredName(target.bukkitPlayer!!)}${CC.GREEN}.")

            target.bukkitPlayer!!.setMetadata(
                "frozen",
                FixedMetadataValue(
                    Lemon.instance, true
                )
            )

            PostFreezeEvent(target.bukkitPlayer!!).dispatch()

            FrozenPlayerHandler.expirables[target.bukkitPlayer!!.uniqueId] = FrozenPlayerHandler.FrozenExpirable()
        }
    }
}
