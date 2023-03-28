package gg.scala.lemon.command.management.invalidation

import com.mongodb.client.model.Filters
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.command.ConsoleCommandSender

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@AutoRegister
object InvalidatePunishmentRangeCommand : ScalaCommand()
{
    @Syntax("<minimum> <maximum> [category]")
    @CommandAlias("ipr|invalidatepunishmentsrange")
    @CommandPermission("op")
    fun onInvalidate(
        sender: ConsoleCommandSender,
        min: Long, max: Long,
        category: PunishmentCategory
    )
    {
        sender.sendMessage("${CC.GOLD}Now fetching punishments...")

        DataStoreObjectControllerCache.findNotNull<Punishment>()
            .mongo()
            .loadAllWithFilter(
                Filters.and(
                    Filters.eq("category", category.name)
                )
            )
            .thenAcceptAsync { punishments ->
                var invalidated = 0

                punishments
                    .filter { it.value.addedAt in (min + 1) until max }
                    .forEach {
                        QuickAccess.attemptRemoval(
                            punishment = it.value,
                            reason = "Manual Invalidation"
                        ); invalidated++
                    }

                sender.sendMessage("${CC.SEC}Invalidated ${CC.PRI}${invalidated}${CC.SEC} punishments globally.")
            }
    }

}
