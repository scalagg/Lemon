package gg.scala.lemon

import com.google.gson.LongSerializationPolicy
import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.acf.BukkitCommandExecutionContext
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.annotations.commands.ManualRegister
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizers
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.command.ScalaCommandManager
import gg.scala.commons.config.annotations.ContainerConfig
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginWebsite
import gg.scala.lemon.adapter.LemonPlayerTypeAdapter
import gg.scala.lemon.adapter.client.PlayerClientAdapter
import gg.scala.lemon.adapter.statistic.ServerStatisticProvider
import gg.scala.lemon.adapter.statistic.impl.DefaultServerStatisticProvider
import gg.scala.lemon.command.customizer.LemonCommandCustomizer
import gg.scala.lemon.handler.DataStoreOrchestrator
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.nametag.DefaultNametagProvider
import gg.scala.lemon.processor.LanguageConfigProcessor
import gg.scala.lemon.processor.SettingsConfigProcessor
import gg.scala.validate.ScalaValidateData
import gg.scala.validate.ScalaValidateUtil
import me.lucko.helper.Events
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.serializers.Serializers.create
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.uuid.UUIDUtil
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

@Plugin(
    name = "Lemon",
    version = "%remote%/%branch%/%id%"
)

@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")

@PluginDependency("scala-commons")
@PluginDependency("store-spigot")

@PluginDependency("spark", soft = true)
@PluginDependency("LunarClient-API", soft = true)
@PluginDependency("PlaceholderAPI", soft = true)
@PluginDependency("cloudsync", soft = true)

@ContainerConfig(
    value = "settings",
    model = SettingsConfigProcessor::class
)
@ContainerConfig(
    value = "language",
    model = LanguageConfigProcessor::class
)
class Lemon : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        lateinit var instance: Lemon
    }

    val settings: SettingsConfigProcessor
        get() = config()

    val languageConfig: LanguageConfigProcessor
        get() = config()

    lateinit var lemonWebData: ScalaValidateData
    lateinit var serverStatisticProvider: ServerStatisticProvider

    val clientAdapters = mutableListOf<PlayerClientAdapter>()
    var initialization = System.currentTimeMillis()

    val aware by lazy {
        AwareBuilder.of<AwareMessage>("lemon")
            .logger(logger)
            .codec(AwareMessageCodec)
            .build()
    }

    @ContainerEnable
    fun containerEnable()
    {
        instance = this
        logger.info("Attempting to load Lemon using provided id.")

        validatePlatformInformation()
        runAfterDataValidation()
    }

    private fun validatePlatformInformation()
    {
        val webData = ScalaValidateUtil.fetchServerData(
            settings.serverPassword,
            settings.serverPasswordHttps,
            settings.serverPasswordSupplier
        )

        if (webData == null)
        {
            logger.severe(
                "Something went wrong during data validation, shutting down... ${
                    "(No information was returned, or \"No result was found.\" was returned.)"
                }"
            )
            server.pluginManager.disablePlugin(this)

            return
        }

        lemonWebData = webData

        logger.info(
            "Loading Lemon with ${lemonWebData.serverName}'s information."
        )
    }

    private fun runAfterDataValidation()
    {
        create {
            setLongSerializationPolicy(LongSerializationPolicy.STRING)
            registerTypeAdapter(
                LemonPlayer::class.java,
                LemonPlayerTypeAdapter
            )
        }

        this.flavor {
            this.inject(DataStoreOrchestrator)
        }

        this.configureHandlers()

        CommandManagerCustomizers
            .default<LemonCommandCustomizer>()

        this.configureQol()

        this.logger.info(
            "Finished Lemon resource initialization in ${
                System.currentTimeMillis() - this.initialization
            }ms"
        )
    }
    private fun configureQol()
    {
        val initialization = System.currentTimeMillis()

        CC.setup(
            toCCColorFormat(lemonWebData.primary),
            toCCColorFormat(lemonWebData.secondary)
        )

        NametagHandler.registerProvider(DefaultNametagProvider)

        Events.subscribe(PlayerInteractAtEntityEvent::class.java)
            .filter { it.rightClicked is Player && it.rightClicked.hasMetadata("frozen") }
            .handler {
                it.player.sendMessage("${CC.RED}You cannot hurt players who are frozen!"); it.isCancelled = true
            }

        Events.subscribe(PlayerMoveEvent::class.java)
            .filter { EventUtils.hasPlayerMoved(it) && it.player.hasMetadata("frozen") }
            .handler { it.player.teleport(it.from) }

        this.serverStatisticProvider =
            DefaultServerStatisticProvider

        logger.info(
            "Finished player QOL initialization in ${
                System.currentTimeMillis() - initialization
            }ms."
        )
    }

    private fun toCCColorFormat(string: String): String
    {
        return ChatColor.valueOf(string).toString()
    }

    private fun configureHandlers()
    {
        flavor {
            inject(RankHandler)
        }

        aware.listen(RedisHandler)

        aware.connect()
            .toCompletableFuture()
            .join()

        logger.info("Setup data storage & distribution controllers.")
    }

    fun parseUniqueIdFromContext(firstArg: String): Pair<UUID, Boolean>
    {
        if (firstArg.length == 32)
        {
            val uniqueId = UUIDUtil.formatUUID(firstArg)
                ?: throw ConditionFailedException(
                    "${CC.YELLOW}${firstArg}${CC.RED} is not a valid UUID."
                )

            return Pair(uniqueId, true)
        } else if (firstArg.length <= 16)
        {
            val uniqueId = ScalaStoreUuidCache
                .uniqueId(firstArg)
                ?: throw ConditionFailedException(
                    "No player with the username matching ${CC.YELLOW}${firstArg}${CC.RED} exists."
                )

            return Pair(uniqueId, false)
        }

        return try
        {
            Pair(UUID.fromString(firstArg)!!, true)
        } catch (ignored: Exception)
        {
            throw ConditionFailedException("${CC.YELLOW}${firstArg}${CC.RED} is not a valid uuid.")
        }
    }
}
