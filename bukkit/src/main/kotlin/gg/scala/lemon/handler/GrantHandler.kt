package gg.scala.lemon.handler

import com.mongodb.client.model.Filters.eq
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.throwAnyExceptions
import gg.scala.lemon.util.QuickAccess.fetchColoredName
import gg.scala.lemon.util.QuickAccess.senderUuid
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.MongoDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import org.bson.conversions.Bson
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object GrantHandler
{
    private fun loadGrantsFilteredBy(
        filter: Bson, test: ((Grant) -> Boolean)? = null
    ): CompletableFuture<List<Grant>>
    {
        val controller = DataStoreObjectControllerCache.findNotNull<Grant>()

        return controller
            .useLayerWithReturn<MongoDataStoreStorageLayer<Grant>, CompletableFuture<List<Grant>>>(
                DataStoreStorageType.MONGO
            ) {
                return@useLayerWithReturn this
                    .loadAllWithFilter(filter)
                    .thenApply {
                        if (test == null)
                            return@thenApply it.values.toList()

                        val mutableList = mutableListOf<Grant>()

                        it.forEach { entry ->
                            if (test.invoke(entry.value))
                            {
                                mutableList.add(entry.value)
                            }
                        }

                        return@thenApply mutableList
                    }
            }
    }

    fun fetchGrantsByExecutor(uuid: UUID): CompletableFuture<List<Grant>>
    {
        return loadGrantsFilteredBy(
            eq("addedBy", uuid.toString())
        )
    }

    fun fetchGrantsFor(uuid: UUID?): CompletableFuture<List<Grant>>
    {
        return loadGrantsFilteredBy(
            eq("target", uuid.toString())
        )
    }

    fun registerGrant(grant: Grant) = grant
        .save().throwAnyExceptions()

    fun invalidateAllGrantsBy(uuid: UUID, sender: CommandSender): CompletableFuture<Void>
    {
        return fetchGrantsByExecutor(uuid)
            .thenAccept {
                handle(uuid, sender, it)
            }
    }

    fun invalidateAllGrantsFor(uuid: UUID, sender: CommandSender): CompletableFuture<Void>
    {
        return fetchGrantsFor(uuid)
            .thenAccept {
                handle(uuid, sender, it)
            }
    }

    fun handle(uuid: UUID, sender: CommandSender, list: List<Grant>)
    {
        this.handleInvalidation(uuid, sender).invoke(list)
    }

    private fun handleInvalidation(uuid: UUID, sender: CommandSender): (List<Grant>) -> Unit
    {
        return { grants ->
            var wiped = 0

            grants.forEach {
                if (!it.isRemoved)
                {
                    it.removedBy = senderUuid(sender)
                    it.removedAt = System.currentTimeMillis()
                    it.removedOn = Lemon.instance.settings.id
                    it.removedReason = "Manual Wipe (Lemon)"

                    it.save()

                    wiped++
                }
            }

            if (wiped == 0)
            {
                sender.sendMessage("${CC.RED}No active grants related to $uuid were found.")
            }
        }
    }

    fun fetchExactGrantById(uuid: UUID): CompletableFuture<Grant?>
    {
        return DataStoreObjectControllerCache
            .findNotNull<Grant>()
            .load(uuid, DataStoreStorageType.MONGO)
    }

    fun handleGrant(sender: CommandSender, grant: Grant)
    {
        grant.save().thenRunAsync {
            sender.sendMessage(
                arrayOf(
                    "${CC.SEC}You've granted ${fetchColoredName(grant.target)}${CC.SEC} the ${
                        grant.getRank().getColoredName()
                    }${CC.SEC} rank for ${CC.WHITE}${grant.addedReason}${CC.SEC}.",
                    "${CC.SEC}Granted for scopes: ${CC.PRI}${
                        grant.scopes.joinToString(
                            separator = "${CC.SEC}, ${CC.PRI}"
                        )
                    }${CC.SEC}.",
                    "${CC.SEC}This grant will ${grant.fancyDurationString}${CC.SEC}${
                        if (!grant.isPermanent) "${CC.GRAY} (on ${grant.expirationString})${CC.SEC}" else ""
                    }."
                )
            )
        }
    }
}
