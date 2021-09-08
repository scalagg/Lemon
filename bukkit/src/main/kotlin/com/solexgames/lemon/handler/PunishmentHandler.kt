package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.punishment.Punishment
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/26/2021
 */

object PunishmentHandler {

    private fun fetchPunishments(test: (Punishment) -> Boolean): CompletableFuture<List<Punishment>> {
        return Lemon.instance.mongoHandler.punishmentLayer.fetchAllEntries().thenApply {
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
        return fetchPunishments {
            it.target == uuid && it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsByExecutorOfIntensity(uuid: UUID, intensity: PunishmentCategoryIntensity): CompletableFuture<List<Punishment>> {
        return fetchPunishments {
            it.addedBy == uuid && it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsForTargetOfCategory(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments {
            it.target == uuid && it.category == category
        }
    }

    fun fetchPunishmentsByExecutorOfCategory(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments {
            it.addedBy == uuid && it.category == category
        }
    }

    fun fetchAllPunishmentsForTarget(uuid: UUID): CompletableFuture<List<Punishment>> {
        return fetchPunishments {
            it.target == uuid
        }
    }

    fun fetchAllPunishmentsByExecutor(uuid: UUID): CompletableFuture<List<Punishment>> {
        return fetchPunishments {
            it.addedBy == uuid
        }
    }

    fun fetchExactPunishmentById(uuid: UUID): CompletableFuture<Punishment> {
        return Lemon.instance.mongoHandler.punishmentLayer.fetchEntryByKey(uuid.toString())
    }

}
