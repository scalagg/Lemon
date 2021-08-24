package com.solexgames.lemon.player.rank

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.util.CC
import java.util.*

class Rank(uuid: UUID = UUID.randomUUID(), name: String) {

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
