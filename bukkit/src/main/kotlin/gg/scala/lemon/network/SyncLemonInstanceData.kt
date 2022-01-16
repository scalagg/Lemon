package gg.scala.lemon.network

import gg.scala.lemon.Lemon
import me.lucko.helper.messaging.InstanceData

/**
 * @author GrowlyX
 * @since 1/16/2022
 */
class SyncLemonInstanceData : InstanceData
{
    override fun getId() = Lemon.instance.settings.id
    override fun getGroups() = mutableSetOf(Lemon.instance.settings.group)
}
