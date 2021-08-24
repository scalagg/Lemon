package com.solexgames.lemon.player.rank

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.util.CC
import java.util.*

class Rank(uuid: UUID = UUID.randomUUID(), name: String) {

    constructor(name: String) : this(UUID.randomUUID(), name)

    var uuid = uuid
    var weight = 0

    var name = name
    var prefix = CC.GRAY
    var suffix = ""
    var color = CC.GRAY

    var italic = false
    var hidden = false
    var defaultRank = false

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

        this.permissions.forEach {
            if (!compoundedPermissions.contains(it)) {
                compoundedPermissions.add(it)
            }
        }

        this.inheritances.forEach {
            val rank = Lemon.instance.rankHandler.getRank(it).orElse(null)

            rank?.permissions?.forEach { otherPermission ->
                if (!compoundedPermissions.contains(otherPermission)) {
                    compoundedPermissions.add(otherPermission)
                }
            }
        }

        return compoundedPermissions
    }
}
