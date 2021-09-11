package com.solexgames.lemon.handler

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.util.CubedCacheUtil
import com.solexgames.lemon.util.quickaccess.coloredName
import com.solexgames.lemon.util.quickaccess.reloadPlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
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

    fun wipeGrant(uuid: UUID, remover: UUID?) {
        fetchExactGrantById(uuid).whenComplete { grant, _ ->
            grant.removedReason = "Removed"
            grant.removedAt = System.currentTimeMillis()
            grant.removed = true
            grant.removedBy = remover

            grant.save()
        }
    }

    fun wipeAllGrantsFor(uuid: UUID) {
        fetchGrantsFor(uuid).whenComplete { grants, _ ->
            grants.forEach {
                it.removedReason = "Removed (Grant Wipe)"
                it.removedAt = System.currentTimeMillis()
                it.removed = true

                it.save()
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
            "${CC.SEC}You've granted ${coloredName(name)}${CC.SEC} the rank ${grant.getRank().getColoredName()}${CC.SEC} for ${CC.WHITE}${grant.addedReason}${CC.SEC}.",
            "${CC.SEC}Granted for scopes: ${CC.PRI}${
                grant.scopes.joinToString(
                    separator = "${CC.SEC}, ${CC.PRI}"
                )
            }${CC.SEC}.",
            "${CC.SEC}The grant will ${grant.getFancyDurationString()}${CC.SEC}."
        ))
    }

}
