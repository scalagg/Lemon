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
        // TODO: 8/24/2021 add Collection#deleteOne stuff
    }

    fun wipeAllGrantsFor(uuid: UUID) {
        // TODO: 8/24/2021 wipe all grants from GrantHandler#findGrants
    }

    /**
     * Find all of a player's grants
     *
     * @param uuid the unique identifier of the user to find it by
     * @return the collection of the grants
     */
    fun findGrants(uuid: UUID): MutableList<Grant> {
        return grants.values.stream()
            .filter { it.target == uuid }
            .sorted(Comparator.comparingInt {
                -it.getRank().weight
            })
            .collect(Collectors.toList())
    }

}
