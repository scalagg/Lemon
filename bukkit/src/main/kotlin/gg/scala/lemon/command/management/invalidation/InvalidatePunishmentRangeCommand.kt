package gg.scala.lemon.command.management.invalidation

import gg.scala.lemon.handler.DataStoreHandler
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.command.ConsoleCommandSender

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object InvalidatePunishmentRangeCommand : BaseCommand() {

    @Syntax("<minimum> <maximum> [category]")
    @CommandAlias("ipr|invalidatepunishmentsrange")
    fun onInvalidate(
        sender: ConsoleCommandSender,
        min: Long, max: Long,
        @Optional category: PunishmentCategory?
    ) {
        sender.sendMessage("${CC.GOLD}Now fetching punishments...")

        DataStoreHandler.punishmentLayer.fetchAllEntries()
            .thenAcceptAsync { punishments ->
                var invalidated = 0

                punishments
                    .filter {
                        if (category == null) {
                            true
                        } else {
                            it.value.category == category
                        }
                    }
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
