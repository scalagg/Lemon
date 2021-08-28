package com.solexgames.lemon.handler

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.punishment.Punishment
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors

object GrantHandler {

    private fun fetchGrants(key: String, uuid: UUID, test: (Grant) -> Boolean): CompletableFuture<List<Grant>> {
        return CompletableFuture.supplyAsync {
            val list = ArrayList<Grant>()

            Lemon.instance.mongoHandler.punishmentCollection.find(Filters.eq(key, uuid.toString())).forEach {
                val grant = LemonConstants.GSON.fromJson(it.toJson(), Grant::class.java)

                if (grant != null && test.invoke(grant)) list.add(grant)
            }

            return@supplyAsync list
        }
    }

    fun fetchGrantsByExecutor(uuid: UUID): CompletableFuture<List<Grant>> {
        return fetchGrants("addedBy", uuid) { true }
    }

    fun fetchGrantsFor(uuid: UUID): CompletableFuture<List<Grant>> {
        return fetchGrants("target", uuid) { true }
    }

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

    fun wipeGrant(uuid: UUID, remover: UUID?) {
        fetchExactGrantById(uuid).whenComplete { grant, throwable ->
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
        return CompletableFuture.supplyAsync {
            val reference = AtomicReference<Grant>()
            val document = Lemon.instance.mongoHandler.grantCollection
                .find(Filters.eq("uuid", uuid.toString())).first()

            if (document != null) {
                val grant = LemonConstants.GSON.fromJson(document.toJson(), Grant::class.java)

                if (grant != null) reference.set(grant)
            }

            return@supplyAsync reference.get()
        }
    }

}
