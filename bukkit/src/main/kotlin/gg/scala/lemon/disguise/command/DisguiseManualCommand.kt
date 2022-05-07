package gg.scala.lemon.disguise.command

import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.lemon.disguise.information.DisguiseInfo
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/30/2021
 */
object DisguiseManualCommand : ScalaCommand()
{
    @CommandAlias("dgm|disguisemanual")
    @CommandCompletion("@all-players @all-players")
    @CommandPermission("lemon.command.disguise.manual")
    fun onDisguiseManual(
        sender: CommandSender, target: OnlinePlayer, uuid: UUID
    )
    {
        DataStoreObjectControllerCache.findNotNull<DisguiseInfo>()
            .load(uuid, DataStoreStorageType.MONGO).thenAccept {
                if (it == null)
                {
                    val username = CubedCacheUtil.fetchName(uuid)
                    val disguiseInfo = DisguiseProvider.fetchDisguiseInfo(username!!, uuid)

                    if (disguiseInfo == null)
                    {
                        sender.sendMessage("${CC.RED}No player matching ${CC.YELLOW}$username${CC.RED} exists.")
                        return@thenAccept
                    }

                    DisguiseProvider.handleDisguiseInternal(target.player, disguiseInfo)
                } else
                {
                    DisguiseProvider.handleDisguiseInternal(target.player, it)
                }
            }
    }
}
