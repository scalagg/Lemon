package gg.scala.lemon

import com.google.gson.LongSerializationPolicy
import gg.scala.banana.Banana
import gg.scala.banana.BananaBuilder
import gg.scala.banana.credentials.BananaCredentials
import gg.scala.banana.options.BananaOptions
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.flavor.Flavor
import gg.scala.flavor.FlavorOptions
import gg.scala.lemon.adapter.LemonPlayerAdapter
import gg.scala.lemon.adapter.ProtocolLibHook
import gg.scala.lemon.adapter.annotation.RequiredPlugin
import gg.scala.lemon.adapter.client.PlayerClientAdapter
import gg.scala.lemon.adapter.statistic.ServerStatisticProvider
import gg.scala.lemon.adapter.statistic.impl.DefaultServerStatisticProvider
import gg.scala.lemon.adapter.statistic.impl.SparkServerStatisticProvider
import gg.scala.lemon.annotation.DoNotRegister
import gg.scala.lemon.command.ColorCommand
import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.lemon.disguise.information.DisguiseInfoProvider
import gg.scala.lemon.disguise.update.DisguiseListener
import gg.scala.lemon.extension.AdditionalFlavorCommands
import gg.scala.lemon.handler.*
import gg.scala.lemon.listener.PlayerListener
import gg.scala.lemon.logger.impl.`object`.ChatAsyncFileLogger
import gg.scala.lemon.logger.impl.`object`.CommandAsyncFileLogger
import gg.scala.lemon.network.SyncLemonInstanceData
import gg.scala.lemon.network.SyncLemonNetwork
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.board.ModModeBoardProvider
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.color.PlayerColorHandler
import gg.scala.lemon.player.entity.superboat.EntitySuperBoatCommand
import gg.scala.lemon.player.extension.PlayerCachingExtension
import gg.scala.lemon.player.extension.network.NetworkOnlineStaffCommand
import gg.scala.lemon.player.nametag.DefaultNametagProvider
import gg.scala.lemon.player.nametag.ModModeNametagProvider
import gg.scala.lemon.player.nametag.VanishNametagProvider
import gg.scala.lemon.player.nametag.command.NametagCommand
import gg.scala.lemon.player.nametag.rainbow.RainbowNametagProvider
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.sorter.ScalaSpigotSorterExtension
import gg.scala.lemon.player.visibility.StaffVisibilityHandler
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.processor.LanguageConfigProcessor
import gg.scala.lemon.processor.SettingsConfigProcessor
import gg.scala.lemon.queue.impl.LemonOutgoingMessageQueue
import gg.scala.lemon.server.ServerInstance
import gg.scala.lemon.task.BukkitInstanceUpdateRunnable
import gg.scala.lemon.task.ResourceUpdateRunnable
import gg.scala.lemon.testing.TestingCommand
import gg.scala.store.connection.redis.impl.details.DataStoreRedisConnectionDetails
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.spigot.ScalaDataStoreSpigot
import gg.scala.store.storage.impl.RedisDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.validate.ScalaValidateData
import gg.scala.validate.ScalaValidateUtil
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.network.AbstractNetwork
import me.lucko.helper.network.modules.FindCommandModule
import me.lucko.helper.network.modules.NetworkStatusModule
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import me.lucko.helper.redis.RedisCredentials
import me.lucko.helper.redis.plugin.HelperRedis
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.BukkitCommandExecutionContext
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.scoreboard.ScoreboardHandler
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.Serializers.useGsonBuilderThenRebuild
import net.evilblock.cubed.store.uuidcache.impl.RedisUUIDCache
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.ClassUtils
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.uuid.UUIDUtil
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
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

    lateinit var banana: Banana
    lateinit var credentials: BananaCredentials

    lateinit var serverLayer: DataStoreObjectController<ServerInstance>
    lateinit var localInstance: ServerInstance

    lateinit var lemonWebData: ScalaValidateData
    lateinit var serverStatisticProvider: ServerStatisticProvider

    lateinit var redisConnectionDetails: DataStoreRedisConnectionDetails

    var network by Delegates.notNull<AbstractNetwork>()

    val clientAdapters = mutableListOf<PlayerClientAdapter>()
    var initialization by Delegates.notNull<Long>()

    val flavor by lazy {
        Flavor.create<Lemon>(
            FlavorOptions(logger)
        )
    }

    override fun enable()
    {
        instance = this
        initialization = System.currentTimeMillis()

        logger.info("Fetching server information using provided password...")

        loadBaseConfigurations()

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

        initialLoadMessageQueues()
        initialLoadPlayerQol()
        initialLoadScheduledTasks()
        initialLoadCommands()

        startUuidCacheImplementation()

        logger.info("Finished Lemon resource initialization in ${
            System.currentTimeMillis() - initialization
        }ms")
    }

    private fun initialLoadScheduledTasks()
    {
        Schedulers.async().runRepeating(
            ResourceUpdateRunnable,
            0L, 20L
        )

        Schedulers.async().runRepeating(
            BukkitInstanceUpdateRunnable,
            0L, 20L
        )
    }

    private fun startUuidCacheImplementation()
    {
        val uuidCacheBanana = BananaBuilder()
            .options(
                BananaOptions(
                    channel = "cubed",
                    gson = Serializers.gson,
                )
            )
            .credentials(
                credentials
            ).build()

        Cubed.instance.uuidCache = RedisUUIDCache(uuidCacheBanana)
        Cubed.instance.uuidCache.load()
    }

    private fun initialLoadMessageQueues()
    {
        LemonOutgoingMessageQueue.start()

        logger.info("Started all outgoing jedis message queues.")
    }

    private fun loadListeners()
    {
        server.pluginManager.registerEvents(PlayerListener, this)
    }

    private fun initialLoadCommands()
    {
        val commandManager = CubedCommandManager(
            plugin = this,
            primary = ChatColor.valueOf(lemonWebData.primary),
            secondary = ChatColor.valueOf(lemonWebData.secondary)
        )

        registerCompletionsAndContexts(commandManager)
        registerCommandsInPackage(commandManager, "gg.scala.lemon.command")

        if (settings.disguiseEnabled)
        {
            registerCommandsInPackage(commandManager, "gg.scala.lemon.disguise.command")
        }

        if (settings.playerColorsEnabled)
        {
            commandManager.registerCommand(ColorCommand())
        }

        if (PlayerCachingExtension.loaded)
        {
            commandManager.registerCommand(NetworkOnlineStaffCommand)
        }

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

        Schedulers.async().runRepeating(FrozenPlayerHandler, 0L, 100L)
        Schedulers.async().runRepeating(FrozenPlayerHandler.FrozenPlayerTick(), 0L, 20L)

        Events.subscribe(PlayerInteractAtEntityEvent::class.java)
            .filter { it.rightClicked is Player && it.rightClicked.hasMetadata("frozen") }
            .handler {
                it.player.sendMessage("${CC.RED}You cannot hurt players who are frozen!"); it.isCancelled = true
            }

        Events.subscribe(PlayerMoveEvent::class.java)
            .filter { EventUtils.hasPlayerMoved(it) && it.player.hasMetadata("frozen") }
            .handler { it.player.teleport(it.from) }

        if (settings.redisCachePlayers)
        {
            flavor.inject(PlayerCachingExtension)
            logger.info("Now memorizing fundamental player data to your redis server.")
        }

        // Loading all default player colors
        if (settings.playerColorsEnabled)
        {
            flavor.inject(PlayerColorHandler)
            logger.info("Loaded default player colors for /colors.")
        }

        try
        {
            Class.forName("ScalaSpigot")

            if (settings.tablistSortingEnabled)
            {
                flavor.inject(ScalaSpigotSorterExtension)
                logger.info("Enabled ScalaSpigot Sorter implementation.")
            }
        } catch (ignored: Exception) { }

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

        try
        {
            flavor.startup()
        } catch (exception: Exception)
        {
            exception.printStackTrace()
        }

        logger.info("Finished player qol initialization in ${
            System.currentTimeMillis() - initialization
        }ms.")
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

    fun loadListenersInPackage(plugin: JavaPlugin, `package`: String)
    {
        ClassUtils.getClassesInPackage(plugin, `package`).forEach {
            try
            {
                server.pluginManager.registerEvents(
                    it.newInstance() as Listener, this
                )
            } catch (e: Exception)
            {
                plugin.logger.severe("Could not instantiate: ${it.simpleName} - ${e.message}")
            }
        }
    }

    private fun loadBaseConfigurations()
    {
        configFactory = ConfigFactory.newFactory(this)

        settings = configFactory.fromFile("settings", SettingsConfigProcessor::class.java)
    }

    private fun initialLoadConfigurations()
    {
        languageConfig = configFactory.fromFile(
            "language", LanguageConfigProcessor::class.java
        )

        convertScalaStoreRedisDetails()
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

        val instanceData = SyncLemonInstanceData()
        val messenger = HelperRedis(helperCredentials)

        network = SyncLemonNetwork(
            messenger, instanceData
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

    private fun convertScalaStoreRedisDetails()
    {
        // Converting the scala-store redis
        // details form to Banana's
        val scalaStoreRedis = ScalaDataStoreSpigot.INSTANCE.redis
        credentials = BananaCredentials(
            scalaStoreRedis.hostname,
            scalaStoreRedis.port,
            scalaStoreRedis.password != null,
            scalaStoreRedis.password ?: ""
        )
    }

    private fun loadHandlers()
    {
        flavor.inject(RankHandler)

        serverLayer = DataStoreObjectControllerCache.create()

        invokeTrackedTask("server instance") {
            localInstance = serverLayer
                .useLayerWithReturn<RedisDataStoreStorageLayer<ServerInstance>, ServerInstance>(DataStoreStorageType.REDIS) {
                    this.loadWithFilterSync { it.serverId == settings.id } ?: ServerInstance(
                        settings.id, settings.group
                    )
                }
        }

        redisConnectionDetails = DataStoreRedisConnectionDetails(
            credentials.address,
            credentials.port,
            credentials.password
        )

        banana = BananaBuilder()
            .options(
                BananaOptions(
                    channel = "lemon:spigot",
                    gson = Serializers.gson,
                )
            )
            .credentials(
                credentials
            )
            .build()

        banana.registerClass(RedisHandler)
        banana.subscribe()

        logger.info("Setup data store controllers.")
    }

    fun registerCommandsInPackage(
        commandManager: CubedCommandManager,
        commandPackage: String
    )
    {
        ClassUtils.getClassesInPackage(
            commandManager.plugin, commandPackage
        ).forEach { clazz ->
            if (clazz.isAnnotationPresent(DoNotRegister::class.java))
                return@forEach

            try
            {
                commandManager.registerCommand(
                    clazz.newInstance() as BaseCommand
                )
            } catch (e: Exception)
            {
                e.printStackTrace()

                if (e.message?.contains("can not access a member of") == true)
                {
                    return
                }

                if (e.message?.contains("$") == true)
                {
                    return
                }

                commandManager.plugin.logger.severe("Could not register ${clazz.simpleName}: ${e.message}")
            }
        }
    }

    fun registerCompletionsAndContexts(commandManager: CubedCommandManager)
    {
        commandManager.commandCompletions.registerAsyncCompletion("ranks") {
            return@registerAsyncCompletion RankHandler.ranks.map { it.value.name }
        }

        commandManager.commandContexts.registerContext(Rank::class.java) {
            val firstArgument = it.popFirstArg()

            return@registerContext RankHandler.findRank(firstArgument)
                ?: throw ConditionFailedException("No rank matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
        }

        commandManager.commandContexts
            .registerContext(AsyncLemonPlayer::class.java) {
                return@registerContext AsyncLemonPlayer.of(
                    parseUniqueIdFromContext(it)
                )
            }

        commandManager.commandContexts.registerContext(Channel::class.java) {
            val firstArgument = it.popFirstArg()

            return@registerContext ChatHandler.findChannel(firstArgument)
                ?: throw ConditionFailedException("No channel matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
        }

        commandManager.commandContexts.registerContext(LemonPlayer::class.java) {
            val firstArgument = it.popFirstArg()
            val lemonPlayerOptional = PlayerHandler.findPlayer(firstArgument)

            if (!lemonPlayerOptional.isPresent)
            {
                throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
            }

            val lemonPlayer = lemonPlayerOptional.orElse(null)
                ?: throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")

            if (it.player != null)
            {
                if (!VisibilityHandler.treatAsOnline(lemonPlayer.bukkitPlayer!!, it.player))
                {
                    throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
                }
            }

            return@registerContext lemonPlayer
        }

        commandManager.commandCompletions.registerAsyncCompletion("all-players") {
            return@registerAsyncCompletion mutableListOf<String>().also {
                Bukkit.getOnlinePlayers()
                    .filter { !it.hasMetadata("vanished") }
                    .forEach { player ->
                        it.add(player.name)
                    }
            }
        }

        commandManager.commandCompletions.registerAsyncCompletion("players") {
            return@registerAsyncCompletion mutableListOf<String>().also {
                Bukkit.getOnlinePlayers()
                    .filter { !it.hasMetadata("vanished") }
                    .forEach { player -> it.add(player.name) }
            }
        }
    }

    fun parseUniqueIdFromContext(context: BukkitCommandExecutionContext): UUID
    {
        val firstArg = context.popFirstArg()

        if (firstArg.length == 32) {
            return UUIDUtil.formatUUID(firstArg)
                ?: throw ConditionFailedException("${CC.YELLOW}${firstArg}${CC.RED} is not a valid uuid.")
        } else if (firstArg.length <= 16) {
            return Cubed.instance.uuidCache.uuid(firstArg) ?: Cubed.instance.uuidCache.fetchUUID(
                firstArg
            )
            ?: throw ConditionFailedException("No player with the username ${CC.YELLOW}${firstArg}${CC.RED} exists.")
        }

        return try {
            UUID.fromString(firstArg)
        } catch (ignored: Exception) {
            throw ConditionFailedException("${CC.YELLOW}${firstArg}${CC.RED} is not a valid uuid.")
        }
    }

    override fun disable()
    {
        banana.useResource {
            it.hdel("lemon:heartbeats", settings.id)
        }
    }
}
