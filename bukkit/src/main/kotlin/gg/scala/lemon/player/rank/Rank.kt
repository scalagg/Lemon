package gg.scala.lemon.player.rank

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.util.type.Savable
import net.evilblock.cubed.util.CC
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

    var visible = true

    val children = ArrayList<UUID>()
    val permissions = ArrayList<String>()

    fun getColoredName(): String {
        return color + name
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

        children.forEach {
            Lemon.instance.rankHandler.findRank(it)?.let { rank ->
                rank.getCompoundedPermissions().forEach { permission ->
                    if (!compoundedPermissions.contains(permission)) {
                        compoundedPermissions.add(permission)
                    }
                }
            }
        }

        return compoundedPermissions
    }

    override fun save(): CompletableFuture<Void> {
        return Lemon.instance.mongoHandler.rankLayer.saveEntry(uuid.toString(), this)
    }

    fun saveAndPushUpdatesGlobally(): CompletableFuture<Void> {
        return this.save().thenApply {
            RedisHandler.buildMessage(
                "rank-update",
                hashMapOf<String, String>().also {
                    it["uniqueId"] = uuid.toString()
                }
            ).publishAsync()

            return@thenApply null
        }
    }
}
