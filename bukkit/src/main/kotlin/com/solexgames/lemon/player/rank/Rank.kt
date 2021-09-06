package com.solexgames.lemon.player.rank

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.type.Savable
import net.evilblock.cubed.util.CC
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture

class Rank(
    val uuid: UUID = UUID.randomUUID(),
    var name: String
): Savable {

    constructor(name: String): this(UUID.randomUUID(), name)

    var weight: Int = 0

    var prefix: String = CC.GRAY
    var suffix: String = CC.RESET
    var color: String = CC.GRAY

    var italic = false
    var hidden = false
    var defaultRank = false

    val inheritances = ArrayList<UUID>()
    val permissions = ArrayList<String>()

    fun getColoredName(): String {
        return color + name
    }

    @JvmName("getNewPrefix")
    fun getPrefix(): String {
        val prefix = ""

        return if (prefix.isBlank() || prefix.isEmpty()) {
            prefix
        } else {
            this.prefix
        }
    }

    /**
     * Returns a list with permissions from
     * all inherited ranks as well as the current rank
     */
    fun getCompoundedPermissions(): ArrayList<String> {
        val compoundedPermissions = ArrayList<String>()

        permissions.forEach {
            if (!compoundedPermissions.contains(it)) {
                compoundedPermissions.add(it)
            }
        }

        inheritances.forEach {
            val rank = Lemon.instance.rankHandler.findRank(it).orElse(null)

            compoundedPermissions.addAll(rank.getCompoundedPermissions())
        }

        return compoundedPermissions
    }

    override fun save(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val document = Document("_id", uuid)
            document["uuid"] = uuid.toString()
            document["name"] = name
            document["weight"] = weight
            document["prefix"] = prefix
            document["suffix"] = suffix
            document["color"] = color
            document["italic"] = italic
            document["hidden"] = hidden
            document["defaultRank"] = defaultRank
            document["inheritances"] = inheritances
            document["permissions"] = permissions

            Lemon.instance.mongoHandler.rankCollection.replaceOne(
                Filters.eq("_id", uuid),
                document, ReplaceOptions().upsert(true)
            )
        }
    }
}
