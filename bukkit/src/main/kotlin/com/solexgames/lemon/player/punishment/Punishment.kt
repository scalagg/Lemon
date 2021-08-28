package com.solexgames.lemon.player.punishment

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import com.solexgames.lemon.util.other.Expireable
import com.solexgames.lemon.util.type.Saveable
import org.bson.Document
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
): Expireable(addedAt, duration), Saveable {

    var removedReason: String? = null
    var removedOn: String? = null
    var removedBy: UUID? = null
    var removedAt: Long = -1
    var removed: Boolean = false

    override fun save(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val document = Document("_id", uuid)
            document["target"] = target.toString()
            document["addedBy"] = addedBy.toString()
            document["addedAt"] = addedAt
            document["addedOn"] = addedOn
            document["addedReason"] = addedReason
            document["duration"] = duration
            document["category"] = category.name
            document["removedReason"] = removedReason
            document["removedOn"] = removedOn
            document["removedBy"] = removedBy.toString()
            document["removedAt"] = removedAt
            document["removed"] = removed

            Lemon.instance.mongoHandler.punishmentCollection.replaceOne(
                Filters.eq("_id", uuid),
                document, ReplaceOptions().upsert(true)
            )
        }
    }

    fun isIntensity(intensity: PunishmentCategoryIntensity): Boolean {
        return category.intensity == intensity
    }
}
