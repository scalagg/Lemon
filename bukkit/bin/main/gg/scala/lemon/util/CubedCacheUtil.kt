package gg.scala.lemon.util

import gg.scala.cache.uuid.ScalaStoreUuidCache
import java.util.*

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object CubedCacheUtil
{
    @JvmStatic
    fun fetchUuid(name: String): UUID?
    {
        return ScalaStoreUuidCache.uniqueId(name)
    }

    @JvmStatic
    fun fetchName(uuid: UUID): String?
    {
        return ScalaStoreUuidCache.username(uuid)
    }
}
