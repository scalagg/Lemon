package gg.scala.lemon.player.punishment

import gg.scala.lemon.Lemon
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.util.other.Expirable
import gg.scala.lemon.util.type.Savable
import java.util.*
import java.util.concurrent.CompletableFuture

class Punishment(
    val uuid: UUID,
    val target: UUID,
    val targetCurrentIp: String?,
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
