package gg.scala.lemon.disguise.command

import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.lemon.disguise.information.DisguiseInfo
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.*
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
@CommandAlias("dga|disguiseadmin")
@CommandPermission("lemon.command.disguise.admin")
object DisguiseAdminCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("add")
    @Description("Add a disguise entry.")
    @CommandCompletion("@players")
    fun onAdd(sender: CommandSender, target: UUID)
    {
        DataStoreObjectControllerCache.findNotNull<DisguiseInfo>()
            .load(target, DataStoreStorageType.REDIS).thenAccept {
                if (it != null)
                {
                    sender.sendMessage("${CC.RED}$target is already stored as a disguise profile.")
                    return@thenAccept
                }

                val disguiseInfo = DisguiseProvider
                    .fetchDisguiseInfo(
                        CubedCacheUtil.fetchName(target)!!, target
                    )!!

                DataStoreObjectControllerCache.findNotNull<DisguiseInfo>()
                    .save(disguiseInfo, DataStoreStorageType.MONGO)

                sender.sendMessage("${CC.GREEN}You've registered the disguise profile ${disguiseInfo.username}.")
            }
    }

    @Subcommand("remove")
    @CommandCompletion("@players")
    @Description("Remove a disguise entry.")
    fun onRemove(sender: CommandSender, uuid: UUID)
    {
        DataStoreObjectControllerCache.findNotNull<DisguiseInfo>()
            .load(uuid, DataStoreStorageType.MONGO).thenAccept {
                if (it == null)
                {
                    sender.sendMessage("${CC.RED}$uuid is not stored as a disguise profile.")
                    return@thenAccept
                }

                DataStoreObjectControllerCache.findNotNull<DisguiseInfo>()
                    .delete(uuid, DataStoreStorageType.MONGO)
                    .thenRun {
                        sender.sendMessage("${CC.GREEN}You've deleted the disguise profile ${it.username}.")
                    }
            }
    }
}
