package com.solexgames.lemon.player.punishment

import com.solexgames.lemon.model.Persistent
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import com.solexgames.lemon.util.Expireable
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture

class Punishment(
    val target: UUID,
    val addedBy: UUID?,
    addedAt: Long,
    val addedOn: String,
    val addedReason: String,
    duration: Long,
    private val category: PunishmentCategory
): Expireable(addedAt, duration), Persistent<Document> {

    var removedReason: String? = null
    var removedOn: String? = null
    var removedBy: UUID? = null
    var removedAt: Long = -1
    var removed: Boolean = false

    override fun load(future: CompletableFuture<Document>) {
        TODO("Not yet implemented")
    }

    override fun save(): CompletableFuture<Void> {
        TODO("Not yet implemented")
    }

    fun isIntensity(intensity: PunishmentCategoryIntensity): Boolean {
        return category.intensity == intensity
    }
}
