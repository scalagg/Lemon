package com.solexgames.lemon.util

import net.evilblock.cubed.Cubed
import java.util.*

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object CubedCacheUtil {

    @JvmStatic
    fun fetchUuidByName(name: String): UUID? {
        return Cubed.instance.uuidCache.uuid(name)
    }

    @JvmStatic
    fun fetchNameByUuid(uuid: UUID): String {
        return Cubed.instance.uuidCache.name(uuid)
    }
}
