package com.solexgames.lemon.handler

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.punishment.Punishment
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import org.bson.conversions.Bson
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
object PunishmentHandler {

    private fun fetchPunishments(filter: Bson, test: (Punishment) -> Boolean): CompletableFuture<List<Punishment>> {
        return Lemon.instance.mongoHandler.punishmentLayer.fetchAllEntriesWithFilter(filter).thenApply {
            val mutableList = mutableListOf<Punishment>()

            it.forEach { entry ->
                if (test.invoke(entry.value)) {
                    mutableList.add(entry.value)
                }
            }

            return@thenApply mutableList
        }
    }

    fun fetchPunishmentsForTargetOfIntensity(uuid: UUID, intensity: PunishmentCategoryIntensity): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) {
            it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsByExecutorOfIntensity(uuid: UUID, intensity: PunishmentCategoryIntensity): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) {
            it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsForTargetOfCategory(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) {
            it.category == category
        }
    }

    fun fetchPunishmentsByExecutorOfCategory(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) {
            it.category == category
        }
    }

    fun fetchAllPunishmentsForTarget(uuid: UUID): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) { true }
    }

    fun fetchAllPunishmentsByExecutor(uuid: UUID): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) { true }
    }

    fun fetchExactPunishmentById(uuid: UUID): CompletableFuture<Punishment> {
        return Lemon.instance.mongoHandler.punishmentLayer.fetchEntryByKey(uuid.toString())
    }

}
