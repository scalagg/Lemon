package com.solexgames.lemon.player.punishment

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import com.solexgames.lemon.util.other.Expirable
import com.solexgames.lemon.util.type.Savable
import java.util.*
import java.util.concurrent.CompletableFuture

class Punishment(
    val uuid: UUID,
    val target: UUID,
    val addedBy: UUID?,
    addedAt: Long,
    val addedOn: String,
    val addedReason: String,
    duration: Long,
    val category: PunishmentCategory
): Expirable(addedAt, duration), Savable {

    var removedReason: String? = null
    var removedOn: String? = null
    var removedBy: UUID? = null
    var removedAt: Long = -1
    var isRemoved: Boolean = false

    val isActive: Boolean
        get() = !isRemoved && !hasExpired

    override fun save(): CompletableFuture<Void> {
        return Lemon.instance.mongoHandler.punishmentLayer.saveEntry(uuid.toString(), this)
    }

    fun isIntensity(intensity: PunishmentCategoryIntensity): Boolean {
        return category.intensity == intensity
    }
}
