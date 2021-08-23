package com.solexgames.lemon.player.rank

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.util.CC
import java.util.*

class Rank(uuid: UUID = UUID.randomUUID(), name: String) {

    constructor(name: String) : this(UUID.randomUUID(), name)

    companion object {
        fun getDefaultRank(): Rank {
            val optionalRank = Lemon.instance.rankHandler.getRank("Default")

            if (optionalRank.isPresent) {
                return optionalRank.get()
            }

            val rank = Rank("Default")
            rank.defaultRank = true

            return rank
        }
    }

    var uuid = uuid
    var weight = 0

    var name = name
    var prefix = CC.GRAY
    var suffix = ""
    var color = CC.GRAY

    var italic = false
    var hidden = false
    var defaultRank = false
    var purchasable = false

    val inheritances: List<UUID> = listOf()
    val permissions: List<String> = listOf()

    fun getColoredName(): String {
        return getItalic() + color + name
    }

    fun getItalic(): String {
        return if (italic) { CC.I } else { "" }
    }

    fun hasPermission(permission: String): Boolean {
        if (permissions.contains(permission.toLowerCase())) {
            return true
        }
        TODO("loop through inheritances and see if they have the permission")
    }
}