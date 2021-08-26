package com.solexgames.lemon.handler

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.grant.Grant
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

object GrantHandler {

    private var grants: MutableMap<UUID, Grant> = mutableMapOf()

    fun registerGrant(uuid: UUID, grant: Grant) {
        CompletableFuture.runAsync {
            Lemon.instance.mongoHandler.grantCollection.replaceOne(
                Filters.eq("_id", uuid),
                Document.parse(LemonConstants.GSON.toJson(grant)),
                ReplaceOptions().upsert(true)
            )
        }.whenComplete { _, u ->
            u?.printStackTrace()
        }
    }

    fun wipeGrant(uuid: UUID) {
        val grant = findGrantById(uuid)

        grant.ifPresent {
            it.removedReason = "Removed"
            it.removedAt = System.currentTimeMillis()
            it.removed = true
            it.save()
        }
    }

    fun wipeAllGrantsFor(uuid: UUID) {
        val grants = findGrants(uuid)

        grants.forEach {
            it.removedReason = "Removed"
            it.removedAt = System.currentTimeMillis()
            it.removed = true
            it.save()
        }
    }

    /**
     * Find a grant by its id
     *
     * @param uuid the unique identifier of the grant
     * @return the grant if found
    */
    private fun findGrantById(uuid: UUID): Optional<Grant> {
        return Optional.ofNullable(grants.getOrDefault(uuid, null))
    }

    /**
     * Find all of a player's grants
     *
     * @param uuid the unique identifier of the user to find it by
     * @return the collection of the grants
     */
    fun findGrants(uuid: UUID): List<Grant> {
        return grants.values.stream()
            .filter { it.target == uuid }
            .sorted(Comparator.comparingInt {
                -it.getRank().weight
            })
            .collect(Collectors.toList())
    }

}
