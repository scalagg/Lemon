package com.solexgames.lemon.punishment

import com.solexgames.lemon.model.Persistent
import com.solexgames.lemon.punishment.category.PunishmentCategory
import com.solexgames.lemon.punishment.category.PunishmentCategoryIntensity
import com.solexgames.lemon.util.Expireable
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture

class Punishment(
    override val addedAt: Long,
    override val duration: Long,

    val creator: UUID,
    val target: UUID,
    val server: String,

    var removed: Boolean,
    var remover: UUID,
    var removalOn: String,
    var removedAt: Long,

    val category: PunishmentCategory
): Expireable(addedAt, duration), Persistent<Document> {

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
