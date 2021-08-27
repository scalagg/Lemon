package com.solexgames.lemon.player.rank

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.type.Persistent
import net.evilblock.cubed.util.CC
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

class Rank(
    val uuid: UUID = UUID.randomUUID(),
    var name: String
): Persistent<Document> {

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

    /**
     * Returns a mutable list with permissions from
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

    override fun load(future: CompletableFuture<Document>) {
        TODO("Not yet implemented")
    }

    override fun save(): CompletableFuture<Void> {
        TODO("Not yet implemented")
    }
}
