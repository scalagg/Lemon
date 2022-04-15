package gg.scala.lemon.disguise.command

import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.lemon.disguise.information.DisguiseInfo
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
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
