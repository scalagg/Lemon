package gg.scala.lemon.disguise.information

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
)
{
    companion object
    {
        @JvmStatic
        val NOTHING: DisguiseInfo? = null
    }
}
