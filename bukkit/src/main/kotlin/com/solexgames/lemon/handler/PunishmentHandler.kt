package com.solexgames.lemon.handler

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.punishment.Punishment
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList

/**
 * @author GrowlyX
 * @since 8/26/2021
 */

object PunishmentHandler {

    private fun fetchPunishments(key: String, uuid: UUID, test: (Punishment) -> Boolean): CompletableFuture<List<Punishment>> {
        return CompletableFuture.supplyAsync {
            val list = ArrayList<Punishment>()

            Lemon.instance.mongoHandler.grantCollection.find(Filters.eq(key, uuid.toString())).forEach {
                val punishment = LemonConstants.GSON.fromJson(it.toJson(), Punishment::class.java)

                if (punishment != null && test.invoke(punishment)) list.add(punishment)
            }

            return@supplyAsync list
        }
    }

    fun fetchPunishmentsForTargetOfIntensity(uuid: UUID, intensity: PunishmentCategoryIntensity): CompletableFuture<List<Punishment>> {
        return fetchPunishments("target", uuid) {
            it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsByExecutorOfIntensity(uuid: UUID, intensity: PunishmentCategoryIntensity): CompletableFuture<List<Punishment>> {
        return fetchPunishments("addedBy", uuid) {
            it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsForTargetOfCategory(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments("target", uuid) {
            it.category == category
        }
    }

    fun fetchPunishmentsByExecutorOfCategory(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments("addedBy", uuid) {
            it.category == category
        }
    }

    fun fetchAllPunishmentsForTarget(uuid: UUID): CompletableFuture<List<Punishment>> {
        return fetchPunishments("target", uuid) { true }
    }

    fun fetchAllPunishmentsByExecutor(uuid: UUID): CompletableFuture<List<Punishment>> {
        return fetchPunishments("addedBy", uuid) { true }
    }

    fun fetchExactPunishmentById(uuid: UUID): CompletableFuture<Punishment> {
        return CompletableFuture.supplyAsync {
            val reference = AtomicReference<Punishment>()
            val document = Lemon.instance.mongoHandler.grantCollection
                .find(Filters.eq("uuid", uuid.toString())).first()

            if (document != null) {
                val punishment = LemonConstants.GSON.fromJson(document.toJson(), Punishment::class.java)

                if (punishment != null) reference.set(punishment)
            }

            return@supplyAsync reference.get()
        }
    }

}
