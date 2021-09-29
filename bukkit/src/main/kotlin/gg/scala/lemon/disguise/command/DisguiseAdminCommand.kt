package gg.scala.lemon.disguise.command

import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.lemon.disguise.information.DisguiseInfo
import gg.scala.lemon.disguise.information.DisguiseInfoProvider
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
@CommandAlias("dga|disguiseadmin")
@CommandPermission("lemon.command.disguise.admin")
class DisguiseAdminCommand : BaseCommand() {

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("add")
    @Syntax("<player>")
    fun onAdd(sender: CommandSender, uuid: UUID) {
        DisguiseInfoProvider.disguiseLayer.fetchEntryByKey(uuid.toString()).thenAccept {
            if (it != null) {
                sender.sendMessage("${CC.RED}$uuid is already stored as a disguise profile.")
                return@thenAccept
            }

            val disguiseInfo = DisguiseProvider.fetchDisguiseInfo(
                CubedCacheUtil.fetchName(uuid)!!, uuid
            )

            DisguiseInfoProvider.disguiseLayer.saveEntry(uuid.toString(), disguiseInfo).thenRun {
                sender.sendMessage("${CC.GREEN}You've registered the disguise profile ${disguiseInfo!!.username}.")
            }
        }
    }

    @Subcommand("remove")
    @Syntax("<player>")
    fun onRemove(sender: CommandSender, uuid: UUID) {
        DisguiseInfoProvider.disguiseLayer.fetchEntryByKey(uuid.toString()).thenAccept {
            if (it == null) {
                sender.sendMessage("${CC.RED}$uuid is not stored as a disguise profile.")
                return@thenAccept
            }

            DisguiseInfoProvider.disguiseLayer.deleteEntry(uuid.toString()).thenRun {
                sender.sendMessage("${CC.GREEN}You've deleted the disguise profile ${it.username}.")
            }
        }
    }
}
