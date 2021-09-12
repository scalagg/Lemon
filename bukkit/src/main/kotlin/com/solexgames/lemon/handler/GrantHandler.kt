package com.solexgames.lemon.handler

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.QuickAccess.coloredName
import com.solexgames.lemon.util.QuickAccess.reloadPlayer
import com.solexgames.lemon.util.QuickAccess.senderUuid
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.util.CubedCacheUtil
import net.evilblock.cubed.util.CC
import org.bson.conversions.Bson
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object GrantHandler {

    private fun fetchGrants(filter: Bson, test: (Grant) -> Boolean): CompletableFuture<List<Grant>> {
        return Lemon.instance.mongoHandler.grantLayer.fetchAllEntriesWithFilter(filter).thenApply {
            val mutableList = mutableListOf<Grant>()

            it.forEach { entry ->
                if (test.invoke(entry.value)) {
                    mutableList.add(entry.value)
                }
            }

            return@thenApply mutableList
        }
    }

    fun fetchGrantsByExecutor(uuid: UUID): CompletableFuture<List<Grant>> {
        return fetchGrants(
            Filters.eq("addedBy", uuid.toString())
        ) { true }
    }

    fun fetchGrantsFor(uuid: UUID): CompletableFuture<List<Grant>> {
        return fetchGrants(
            Filters.eq("target", uuid.toString())
        ) { true }
    }

    fun registerGrant(grant: Grant) {
        grant.save().whenComplete { _, u ->
            u?.printStackTrace()
        }
    }

    fun invalidateAllGrantsBy(uuid: UUID, sender: CommandSender): CompletableFuture<Void> {
        return fetchGrantsByExecutor(uuid).thenApply { handle(uuid, sender, it); return@thenApply null }
    }

    fun invalidateAllGrantsFor(uuid: UUID, sender: CommandSender): CompletableFuture<Void> {
        return fetchGrantsFor(uuid).thenApply { handle(uuid, sender, it); return@thenApply null }
    }

    fun handle(uuid: UUID, sender: CommandSender, list: List<Grant>) {
        this.handleInvalidation(uuid, sender).invoke(list)
    }

    private fun handleInvalidation(uuid: UUID, sender: CommandSender): (List<Grant>) -> Unit {
        return { grants ->
            var wiped = 0

            grants.forEach {
                if (!it.isRemoved) {
                    it.isRemoved = true
                    it.removedBy = senderUuid(sender)
                    it.removedAt = System.currentTimeMillis()
                    it.removedOn = Lemon.instance.settings.id
                    it.removedReason = "Manual Wipe (Lemon)"

                    it.save()

                    wiped++
                }
            }

            if (wiped == 0) {
                sender.sendMessage("${CC.RED}No active grants related to $uuid were found.")
            }
        }
    }

    fun fetchExactGrantById(uuid: UUID): CompletableFuture<Grant> {
        return Lemon.instance.mongoHandler.grantLayer
            .fetchEntryByKey(uuid.toString())
    }

    fun handleGrant(sender: CommandSender, grant: Grant) {
        val name = CubedCacheUtil.fetchName(grant.target)

        grant.save().thenRun {
            reloadPlayer(grant.uuid)
        }

        sender.sendMessage(arrayOf(
            "${CC.SEC}You've granted ${coloredName(name)}${CC.SEC} the ${grant.getRank().getColoredName()}${CC.SEC} rank for ${CC.WHITE}${grant.addedReason}${CC.SEC}.",
            "${CC.SEC}Granted for scopes: ${CC.PRI}${
                grant.scopes.joinToString(
                    separator = "${CC.SEC}, ${CC.PRI}"
                )
            }${CC.SEC}.",
            "${CC.SEC}This grant will ${grant.fancyDurationString}${CC.SEC}."
        ))
    }

}
