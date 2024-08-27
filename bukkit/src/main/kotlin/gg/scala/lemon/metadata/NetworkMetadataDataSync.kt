package gg.scala.lemon.metadata

import gg.scala.common.metadata.LegacyRESTMetadataProvider
import gg.scala.common.metadata.NetworkMetadata
import gg.scala.common.metadata.NetworkProperties
import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.config
import net.evilblock.cubed.util.CC
import net.kyori.adventure.key.Key
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 8/27/2024
 */
@Service
object NetworkMetadataDataSync : DataSyncService<NetworkMetadata>()
{
    object NetworkMetadataKeys : DataSyncKeys
    {
        override fun store() = Key.key("network", "metadata")
        override fun newStore() = "network-metadata"
        override fun sync() = Key.key("network", "metasync")
    }

    override fun keys() = NetworkMetadataKeys
    override fun type() = NetworkMetadata::class.java

    override fun postReload()
    {
        val cachedModel = cached()
        val plugin = Lemon.instance
        if (!cachedModel.initialSaveComplete)
        {
            val metadata = LegacyRESTMetadataProvider.fetchServerData(
                plugin.settings.serverPassword,
                plugin.settings.serverPasswordHttps,
                plugin.settings.serverPasswordSupplier
            )

            if (metadata != null)
            {
                plugin.logger.info("Detected unmigrated network metadata service... starting migrations...")
                cachedModel.apply {
                    discord = metadata.discord
                    twitter = metadata.twitter
                    domain = metadata.domain
                    primary = metadata.primary
                    secondary = metadata.secondary
                    serverName = metadata.serverName
                    store = metadata.store
                    properties = NetworkProperties(
                        forbiddenCommands = config()
                            .blacklistedCommands
                            .toMutableSet()
                    )
                    initialSaveComplete = true
                }
                sync(cachedModel)
            } else
            {
                plugin.logger.info("Failed to grab data...")
            }
        }

        CC.setup(
            cachedModel.primary.toChatColor(),
            cachedModel.secondary.toChatColor()
        )

        plugin.logger.info("Updated local color scheme to match network metadata!")
    }

    @JvmStatic
    fun metadata() = cached()

    @JvmStatic
    fun serverName() = cached().serverName

    override fun locatedIn() = DataSyncSource.Mongo

    private fun String.toChatColor() = ChatColor.valueOf(this).toString()
}
