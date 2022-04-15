package gg.scala.lemon

import com.google.gson.LongSerializationPolicy
import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.commands.ManualRegister
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizers
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.flavor.Flavor
import gg.scala.flavor.FlavorOptions
import gg.scala.lemon.adapter.LemonPlayerAdapter
import gg.scala.lemon.adapter.ProtocolLibHook
import gg.scala.lemon.adapter.annotation.RequiredPlugin
import gg.scala.lemon.adapter.client.PlayerClientAdapter
import gg.scala.lemon.adapter.statistic.ServerStatisticProvider
import gg.scala.lemon.adapter.statistic.impl.DefaultServerStatisticProvider
import gg.scala.lemon.adapter.statistic.impl.SparkServerStatisticProvider
import gg.scala.lemon.command.ColorCommand
import gg.scala.lemon.customizer.LemonCommandCustomizer
import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.lemon.disguise.command.DisguiseAdminCommand
import gg.scala.lemon.disguise.command.DisguiseCheckCommand
import gg.scala.lemon.disguise.command.DisguiseCommand
import gg.scala.lemon.disguise.command.DisguiseManualCommand
import gg.scala.lemon.disguise.information.DisguiseInfoProvider
import gg.scala.lemon.disguise.update.DisguiseListener
import gg.scala.lemon.extension.AdditionalFlavorCommands
import gg.scala.lemon.handler.*
import gg.scala.lemon.handler.frozen.FrozenPlayerHandler
import gg.scala.lemon.listener.PlayerListener
import gg.scala.lemon.logger.impl.`object`.ChatAsyncFileLogger
import gg.scala.lemon.logger.impl.`object`.CommandAsyncFileLogger
import gg.scala.lemon.network.SyncLemonInstanceData
import gg.scala.lemon.network.SyncLemonNetwork
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.board.ModModeBoardProvider
import gg.scala.lemon.player.color.PlayerColorHandler
import gg.scala.lemon.player.entity.superboat.EntitySuperBoatCommand
import gg.scala.lemon.player.extension.PlayerCachingExtension
import gg.scala.lemon.player.extension.network.NetworkOnlineStaffCommand
import gg.scala.lemon.player.nametag.DefaultNametagProvider
import gg.scala.lemon.player.nametag.ModModeNametagProvider
import gg.scala.lemon.player.nametag.VanishNametagProvider
import gg.scala.lemon.player.nametag.command.NametagCommand
import gg.scala.lemon.player.nametag.rainbow.RainbowNametagProvider
import gg.scala.lemon.player.sorter.ScalaSpigotSorterExtension
import gg.scala.lemon.player.visibility.StaffVisibilityHandler
import gg.scala.lemon.processor.LanguageConfigProcessor
import gg.scala.lemon.processor.SettingsConfigProcessor
import gg.scala.lemon.server.ServerInstance
import gg.scala.lemon.testing.TestingCommand
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.spigot.ScalaDataStoreSpigot
import gg.scala.store.storage.impl.RedisDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.validate.ScalaValidateData
import gg.scala.validate.ScalaValidateUtil
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.messaging.Messenger
import me.lucko.helper.network.AbstractNetwork
import me.lucko.helper.network.modules.FindCommandModule
import me.lucko.helper.network.modules.NetworkStatusModule
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import me.lucko.helper.redis.RedisCredentials
import me.lucko.helper.redis.plugin.HelperRedis
import net.evilblock.cubed.acf.BukkitCommandExecutionContext
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.scoreboard.ScoreboardHandler
import net.evilblock.cubed.serializers.Serializers.useGsonBuilderThenRebuild
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.ClassUtils
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.uuid.UUIDUtil
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import xyz.mkotb.configapi.ConfigFactory
import java.util.*
import kotlin.properties.Delegates

@Plugin(
    name = Lemon.NAME,
    description = Lemon.DESCRIPTION,
    depends = [
        PluginDependency("Cubed"),
        PluginDependency("helper"),
        PluginDependency("store-spigot"),
        PluginDependency(
            "spark", soft = true
        ),
        PluginDependency(
            "LunarClient-API", soft = true
        ),
        PluginDependency(
            "PlaceholderAPI", soft = true
        )
    ]
)
class Lemon : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        lateinit var instance: Lemon

        const val NAME = "Lemon"
        const val DESCRIPTION = "An extensive punishment, moderation, security and rank suite."
    }

    lateinit var settings: SettingsConfigProcessor
    lateinit var languageConfig: LanguageConfigProcessor

    lateinit var configFactory: ConfigFactory

    lateinit var serverLayer: DataStoreObjectController<ServerInstance>
    lateinit var localInstance: ServerInstance

    lateinit var lemonWebData: ScalaValidateData
    lateinit var serverStatisticProvider: ServerStatisticProvider

    var network by Delegates.notNull<AbstractNetwork>()

    val clientAdapters = mutableListOf<PlayerClientAdapter>()

    var initialization = System.currentTimeMillis()
    var messenger by Delegates.notNull<Messenger>()

    val aware by lazy {
        AwareBuilder.of<AwareMessage>("lemon")
            .logger(logger)
            .codec(AwareMessageCodec)
            .build()
    }

    val flavor by lazy {
        Flavor.create<Lemon>(
            FlavorOptions(logger)
        )
    }

    @ContainerEnable
    fun containerEnable()
    {
        instance = this

        logger.info("Initializing config factory...")

        configFactory = ConfigFactory
            .newFactory(this)

        settings = configFactory
            .fromFile(
                "settings",
                SettingsConfigProcessor::class.java
            )

        logger.info("Attempting to load Lemon using provider password...")

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
            "Now loading Lemon with ${lemonWebData.serverName}'s information..."
        )
    }

    private fun runAfterDataValidation()
    {
        useGsonBuilderThenRebuild {
            it.setLongSerializationPolicy(LongSerializationPolicy.STRING)
                .registerTypeAdapter(LemonPlayer::class.java, LemonPlayerAdapter)
        }

        initialLoadConfigurations()

        flavor.inject(DataStoreOrchestrator)

        loadListeners()
        loadHandlers()

        CommandManagerCustomizers
            .default<LemonCommandCustomizer>()

        initialLoadPlayerQol()

        this.localInstance.metaData = mutableMapOf()
        this.localInstance.metaData["init"] = this.initialization.toString()

        this.logger.info(
            "Finished Lemon resource initialization in ${
                System.currentTimeMillis() - this.initialization
            }ms"
        )
    }

    private fun loadListeners()
    {
        flavor.bind<Lemon>() to this
        flavor.inject(PlayerListener)
    }

    @ManualRegister
    private fun manualRegister(
        commandManager: CubedCommandManager
    )
    {
        if (settings.disguiseEnabled)
        {
            commandManager.registerCommand(DisguiseAdminCommand)
            commandManager.registerCommand(DisguiseCheckCommand)
            commandManager.registerCommand(DisguiseCommand)
            commandManager.registerCommand(DisguiseManualCommand)
        }

        if (settings.playerColorsEnabled)
        {
            commandManager.registerCommand(ColorCommand)
        }

        commandManager.registerCommand(NetworkOnlineStaffCommand)

        commandManager.registerCommand(TestingCommand)
        commandManager.registerCommand(NametagCommand)
        commandManager.registerCommand(EntitySuperBoatCommand)

        commandManager.registerCommand(
            AdditionalFlavorCommands(flavor, this)
        )
    }

    private fun initialLoadPlayerQol()
    {
        val initialization = System.currentTimeMillis()

        CC.setup(
            toCCColorFormat(lemonWebData.primary),
            toCCColorFormat(lemonWebData.secondary)
        )

        NametagHandler.registerProvider(DefaultNametagProvider)
        NametagHandler.registerProvider(VanishNametagProvider)
        NametagHandler.registerProvider(ModModeNametagProvider)

        NametagHandler.registerProvider(RainbowNametagProvider)

        ScoreboardHandler.scoreboardOverride = ModModeBoardProvider

        VisibilityHandler.registerAdapter("staff", StaffVisibilityHandler)

        if (settings.disguiseEnabled)
        {
            flavor.inject(DisguiseInfoProvider)
            flavor.inject(DisguiseProvider)

            server.pluginManager.registerEvents(DisguiseListener, this)

            logger.info("Loaded disguise resources.")
        }

        if (settings.logDataToFile)
        {
            ChatAsyncFileLogger.initialize()
            CommandAsyncFileLogger.initialize()

            logger.info("Started log queue for chat & commands.")
        }

        Events.subscribe(PlayerInteractAtEntityEvent::class.java)
            .filter { it.rightClicked is Player && it.rightClicked.hasMetadata("frozen") }
            .handler {
                it.player.sendMessage("${CC.RED}You cannot hurt players who are frozen!"); it.isCancelled = true
            }

        Events.subscribe(PlayerMoveEvent::class.java)
            .filter { EventUtils.hasPlayerMoved(it) && it.player.hasMetadata("frozen") }
            .handler { it.player.teleport(it.from) }

        flavor.inject(PlayerCachingExtension)
        logger.info("Memorizing fundamental player data to your redis server.")

        // Loading all default player colors
        if (settings.playerColorsEnabled)
        {
            flavor.inject(PlayerColorHandler)
            logger.info("Loaded default player colors for /colors.")
        }

        kotlin.runCatching {
            Class.forName("ScalaSpigot")

            if (settings.tablistSortingEnabled)
            {
                flavor.inject(ScalaSpigotSorterExtension)
                logger.info("Enabled ScalaSpigot Sorter implementation.")
            }
        }

        // filter through the different client implementations
        // & register the ones which have the plugins enabled
        findClassesWithinPackageWithPluginEnabled(
            "gg.scala.lemon.adapter.client.impl"
        ).forEach {
            try
            {
                val clientAdapter = it.newInstance() as PlayerClientAdapter
                clientAdapters.add(clientAdapter)

                logger.info(
                    "${clientAdapter.getClientName()} implementation has been enabled."
                )
            } catch (ignored: Exception)
            {
                logger.info("Failed to instantiate PlayerClientAdapter: ${it.simpleName}.kt")
            }
        }

        if (server.pluginManager.getPlugin("ProtocolLib") != null)
        {
            flavor.inject(ProtocolLibHook)
            logger.info("Now handling tab-completion through ProtocolLib.")
        }

        serverStatisticProvider = DefaultServerStatisticProvider

        if (server.pluginManager.getPlugin("spark") != null)
        {
            serverStatisticProvider = SparkServerStatisticProvider()

            logger.info("Now utilizing spark for server statistics.")
        }

        flavor.startup()

        logger.info(
            "Finished player QOL initialization in ${
                System.currentTimeMillis() - initialization
            }ms."
        )
    }

    private fun findClassesWithinPackageWithPluginEnabled(`package`: String): List<Class<*>>
    {
        return try
        {
            ClassUtils.getClassesInPackage(
                this, `package`
            ).filter {
                server.pluginManager.getPlugin(
                    it.getAnnotation(RequiredPlugin::class.java).value
                ) != null
            }
        } catch (ignored: Exception)
        {
            emptyList()
        }
    }

    private fun toCCColorFormat(string: String): String
    {
        return ChatColor.valueOf(string).toString()
    }

    private fun initialLoadConfigurations()
    {
        languageConfig = configFactory.fromFile(
            "language", LanguageConfigProcessor::class.java
        )

        configureHelperCommunications()
    }

    private fun configureHelperCommunications()
    {
        val scalaStoreRedis = ScalaDataStoreSpigot.INSTANCE.redis

        val helperCredentials = if (scalaStoreRedis.password == null)
        {
            RedisCredentials.of(
                scalaStoreRedis.hostname, scalaStoreRedis.port
            )
        } else
        {
            RedisCredentials.of(
                scalaStoreRedis.hostname, scalaStoreRedis.port, scalaStoreRedis.password
            )
        }

        val instanceData = SyncLemonInstanceData
        messenger = HelperRedis(helperCredentials)

        network = SyncLemonNetwork(
            messenger as HelperRedis,
            instanceData
        )
        network.bindWith(this)

        listOf(
            FindCommandModule(network),
            NetworkStatusModule(network)
        ).forEach {
            it.apply {
                bindModuleWith(this@Lemon)
                setup(this@Lemon)
            }
        }
    }

    private fun loadHandlers()
    {
        flavor.inject(RankHandler)

        serverLayer =
            DataStoreObjectControllerCache.create()

        aware.listen(RedisHandler)

        aware.connect()
            .toCompletableFuture()
            .join()

        localInstance = serverLayer
            .useLayerWithReturn<RedisDataStoreStorageLayer<ServerInstance>, ServerInstance>(DataStoreStorageType.REDIS) {
                this.loadWithFilterSync {
                    it.serverId.equals(settings.id, true)
                } ?: ServerInstance(
                    settings.id, settings.group
                )
            }

        logger.info("Setup data storage & distribution controllers.")
    }

    fun parseUniqueIdFromContext(context: BukkitCommandExecutionContext): Pair<UUID, Boolean>
    {
        val firstArg = context.popFirstArg()

        if (firstArg.length == 32)
        {
            val uniqueId = UUIDUtil.formatUUID(firstArg)
                ?: throw ConditionFailedException(
                    "${CC.YELLOW}${firstArg}${CC.RED} is not a valid uuid."
                )

            return Pair(uniqueId, true)
        } else if (firstArg.length <= 16)
        {
            val uniqueId = ScalaStoreUuidCache
                .uniqueId(firstArg)
                ?: throw ConditionFailedException(
                    "No player with the username ${CC.YELLOW}${firstArg}${CC.RED} exists."
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

    @ContainerDisable
    fun containerDisable()
    {
        aware.shutdown()

        val controller = DataStoreObjectControllerCache
            .findNotNull<ServerInstance>()

        controller.delete(
            localInstance.identifier,
            DataStoreStorageType.REDIS
        ).join()
    }
}
