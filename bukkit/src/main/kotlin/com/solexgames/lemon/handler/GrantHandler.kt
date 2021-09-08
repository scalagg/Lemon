package com.solexgames.lemon.handler

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.grant.Grant
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

object GrantHandler {

    private fun fetchGrants(test: (Grant) -> Boolean): CompletableFuture<List<Grant>> {
        return Lemon.instance.mongoHandler.grantLayer.fetchAllEntries().thenApply {
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
        return fetchGrants {
            it.addedBy == uuid
        }
    }

    fun fetchGrantsFor(uuid: UUID): CompletableFuture<List<Grant>> {
        return fetchGrants {
            it.target == uuid
        }
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

}
