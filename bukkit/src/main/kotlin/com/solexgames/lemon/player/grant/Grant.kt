package com.solexgames.lemon.player.grant

import com.google.gson.annotations.SerializedName
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.model.Saveable
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.lemon.util.Expireable
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture

class Grant(
    @SerializedName("_id") val uuid: UUID,
    var target: UUID,
    var rankId: UUID,
    var addedBy: UUID?,
    addedAt: Long,
    var addedOn: String,
    var addedReason: String,
    duration: Long
) : Expireable(addedAt, duration), Saveable {

    var scopes: MutableList<String> = mutableListOf("global")

    var removedReason: String? = null
    var removedBy: UUID? = null
    var removedAt: Long = -1
    var removed: Boolean = false

    fun getRank(): Rank {
        return Lemon.instance.rankHandler.findRank(rankId).orElse(Lemon.instance.rankHandler.getDefaultRank())
    }

    /**
     * Check if this grant has a scope
     * which matches the current server
     */
    fun isApplicable(): Boolean {
        if (scopes.contains("global")) {
            return true
        }

        var boolean = false

        scopes.forEach {
            if (Lemon.instance.settings.id.equals(it, true)) {
                boolean = true
                return@forEach
            }
        }

        return boolean
    }

    override fun save(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            Lemon.instance.mongoHandler.grantCollection.replaceOne(
                Filters.eq("_id", uuid),
                Document.parse(LemonConstants.GSON.toJson(this)),
                ReplaceOptions().upsert(true)
            )
        }
    }
}