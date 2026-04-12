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
import gg.scala.lemon.util.formatRichText
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.kyori.adventure.key.Key
import org.bukkit.ChatColor
import java.util.Locale

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
                            .toMutableSet(),
                        allowedCommandsWhenBanned = config()
                            .allowedCommandsWhenBanned
                            .toMutableSet()
                    )
                    initialSaveComplete = true
                }
                sync(cachedModel)
            } else
            {
                plugin.logger.info("Failed to grab data... persisting current model")

                cachedModel.apply {
                    initialSaveComplete = true
                }
                sync(cachedModel)
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

    private fun String.toChatColor(): String
    {
        val normalized = formatRichText()
        val translated = Color.translate(normalized)

        return kotlin.runCatching {
            ChatColor.valueOf(this.uppercase(Locale.getDefault())).toString()
        }.getOrElse {
            nearestChatColor(translated).toString()
        }
    }

    private fun nearestChatColor(input: String): ChatColor
    {
        val values = linkedMapOf(
            ChatColor.DARK_RED to intArrayOf(170, 0, 0),
            ChatColor.RED to intArrayOf(255, 85, 85),
            ChatColor.GOLD to intArrayOf(255, 170, 0),
            ChatColor.YELLOW to intArrayOf(255, 255, 85),
            ChatColor.GREEN to intArrayOf(85, 255, 85),
            ChatColor.DARK_GREEN to intArrayOf(0, 170, 0),
            ChatColor.DARK_AQUA to intArrayOf(0, 170, 170),
            ChatColor.AQUA to intArrayOf(85, 255, 255),
            ChatColor.BLUE to intArrayOf(85, 85, 255),
            ChatColor.DARK_PURPLE to intArrayOf(170, 0, 170),
            ChatColor.LIGHT_PURPLE to intArrayOf(255, 85, 255),
            ChatColor.WHITE to intArrayOf(255, 255, 255),
            ChatColor.GRAY to intArrayOf(170, 170, 170),
            ChatColor.DARK_GRAY to intArrayOf(85, 85, 85),
            ChatColor.BLACK to intArrayOf(0, 0, 0)
        )

        val normalized = input.removePrefix("§x").replace("§", "")
        if (normalized.length >= 6 && normalized.take(6).all { it.isDigit() || it.lowercaseChar() in 'a'..'f' })
        {
            val red = normalized.substring(0, 2).toInt(16)
            val green = normalized.substring(2, 4).toInt(16)
            val blue = normalized.substring(4, 6).toInt(16)

            return values.minByOrNull { (_, color) ->
                val redDiff = red - color[0]
                val greenDiff = green - color[1]
                val blueDiff = blue - color[2]
                redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff
            }?.key ?: ChatColor.WHITE
        }

        return ChatColor.WHITE
    }
}
