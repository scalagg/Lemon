package gg.scala.lemon.disguise.information

import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
data class DisguiseInfo(
    val uuid: UUID,
    val username: String,
    val skinInfo: String,
    val skinSignature: String
) : IDataStoreObject
{
    override val identifier: UUID
        get() = uuid

    companion object
    {
        @JvmStatic
        val NOTHING: DisguiseInfo? = null
    }
}
