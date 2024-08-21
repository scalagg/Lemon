package gg.scala.lemon.filter.ml

import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.flavor.service.Service
import net.kyori.adventure.key.Key

/**
 * @author GrowlyX
 * @since 8/21/2024
 */
@Service
object ChatMLDataSync : DataSyncService<ChatMLConfig>()
{
    object ChatMLDataSyncKeys : DataSyncKeys
    {
        override fun store() = Key.key("global", "chatml")
        override fun sync() = Key.key("global", "mlsync")
        override fun newStore() = "chat-ml"
    }

    override fun keys() = ChatMLDataSyncKeys
    override fun type() = ChatMLConfig::class.java
}
