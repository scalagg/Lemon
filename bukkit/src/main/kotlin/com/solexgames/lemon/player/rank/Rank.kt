package com.solexgames.lemon.player.rank

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.type.Persistent
import net.evilblock.cubed.util.CC
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture

class Rank(
    uuid: UUID = UUID.randomUUID(),
    name: String
) : Persistent<Document> {

    constructor(name: String) : this(UUID.randomUUID(), name)

    var uuid: UUID = uuid
    var weight: Int = 0

    var name: String = name
    var prefix: String = CC.GRAY
    var suffix: String = ""
    var color: String = CC.GRAY

    var italic: Boolean = false
    var hidden: Boolean = false
    var defaultRank: Boolean = false

    val inheritances: List<UUID> = listOf()
    val permissions: List<String> = listOf()

    fun getColoredName(): String {
        return color + name
    }

    /**
     * Returns a mutable list with permissions from
     * all inherited ranks as well as the current rank
     */
    fun getCompoundedPermissions(): MutableList<String> {
        val compoundedPermissions = mutableListOf<String>()

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

    override fun load(future: CompletableFuture<Document>) {
        TODO("Not yet implemented")
    }

    override fun save(): CompletableFuture<Void> {
        TODO("Not yet implemented")
    }
}
