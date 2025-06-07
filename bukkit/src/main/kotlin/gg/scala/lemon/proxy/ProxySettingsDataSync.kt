package gg.scala.lemon.proxy

import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.service.Service
import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 1/19/2024
 */
@Service
object ProxySettingsDataSync : DataSyncService<ProxySettings>()
{
    object ProxyDataSyncKeys : DataSyncKeys
    {
        override fun newStore() = "cocoa-settings"

        override fun store() = Key.key("cocoa", "settings")
        override fun sync() = Key.key("cocoa", "settingsync")
    }

    override fun locatedIn() = DataSyncSource.Mongo

    override fun keys() = ProxyDataSyncKeys
    override fun type() = ProxySettings::class.java
}
