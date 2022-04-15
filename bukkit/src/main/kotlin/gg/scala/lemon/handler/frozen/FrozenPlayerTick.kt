package gg.scala.lemon.handler.frozen

import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.lemon.handler.frozen.FrozenPlayerHandler.expirables
import gg.scala.lemon.util.QuickAccess
import me.lucko.helper.promise.ThreadContext
import net.evilblock.cubed.util.CC
import java.util.*

/**
 * @author GrowlyX
 * @since 4/14/2022
 */
@Repeating(20L, context = ThreadContext.ASYNC)
object FrozenPlayerTick : Runnable
{
    override fun run()
    {
        val finalMap = hashMapOf<UUID, FrozenPlayerHandler.FrozenExpirable>().also {
            expirables.forEach { entry ->
                it[entry.key] = entry.value
            }
        }

        finalMap.forEach { (uuid, expirable) ->
            if (expirable.hasExpired)
            {
                QuickAccess.sendStaffMessage(
                    "${CC.AQUA}${QuickAccess.coloredName(uuid)}${CC.D_AQUA} has been frozen for 5 minutes.", true
                )

                expirables.remove(uuid)
            }
        }
    }
}
