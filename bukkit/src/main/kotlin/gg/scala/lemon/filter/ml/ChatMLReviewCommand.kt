package gg.scala.lemon.filter.ml

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType

/**
 * @author GrowlyX
 * @since 8/23/2024
 */
@AutoRegister
object ChatMLReviewCommand : ScalaCommand()
{
    @CommandAlias("chatml-review")
    @CommandPermission("lemon.command.chatml.review")
    fun review(player: ScalaPlayer) = DataStoreObjectControllerCache
        .findNotNull<ChatMLPunishmentAudit>()
        .loadAll(DataStoreStorageType.MONGO)
        .thenAccept {
            ChatMLPunishmentAuditReviewMenu(entries = it.values.toList()).openMenu(player.bukkit())
        }
}
