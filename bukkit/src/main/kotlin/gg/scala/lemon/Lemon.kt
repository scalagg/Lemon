package gg.scala.lemon

import com.google.gson.LongSerializationPolicy
import com.solexgames.datastore.commons.connection.impl.RedisConnection
import com.solexgames.datastore.commons.connection.impl.redis.AuthRedisConnection
import com.solexgames.datastore.commons.connection.impl.redis.NoAuthRedisConnection
import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import com.solexgames.datastore.commons.storage.impl.RedisStorageBuilder
import gg.scala.banana.Banana
import gg.scala.banana.BananaBuilder
import gg.scala.banana.credentials.BananaCredentials
import gg.scala.banana.options.BananaOptions
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.lemon.adapter.LemonPlayerAdapter
import gg.scala.lemon.adapter.ProtocolLibHook
import gg.scala.lemon.adapter.UUIDAdapter
import gg.scala.lemon.adapter.client.ClientMetadata
import gg.scala.lemon.adapter.client.PlayerClientAdapter
import gg.scala.lemon.adapter.statistic.ServerStatisticProvider
import gg.scala.lemon.adapter.statistic.impl.DefaultSparkServerStatisticProvider
import gg.scala.lemon.adapter.statistic.impl.SparkServerStatisticProvider
import gg.scala.lemon.annotation.DoNotRegister
import gg.scala.lemon.command.ColorCommand
import gg.scala.lemon.cooldown.CooldownHandler
import gg.scala.lemon.handler.LemonCooldownHandler
import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.lemon.disguise.information.DisguiseInfoProvider
import gg.scala.lemon.disguise.update.DisguiseListener
import gg.scala.lemon.handler.*
import gg.scala.lemon.listener.PlayerListener
import gg.scala.lemon.logger.impl.`object`.ChatAsyncFileLogger
import gg.scala.lemon.logger.impl.`object`.CommandAsyncFileLogger
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.board.ModModeBoardProvider
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.color.PlayerColorHandler
import gg.scala.lemon.player.extension.PlayerCachingExtension
import gg.scala.lemon.player.nametag.DefaultNametagProvider
import gg.scala.lemon.player.nametag.ModModeNametagProvider
import gg.scala.lemon.player.nametag.VanishNametagProvider
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.visibility.StaffVisibilityHandler
import gg.scala.lemon.processor.LanguageConfigProcessor
import gg.scala.lemon.processor.MongoDBConfigProcessor
import gg.scala.lemon.processor.SettingsConfigProcessor
import gg.scala.lemon.queue.impl.LemonOutgoingMessageQueue
import gg.scala.lemon.server.ServerInstance
import gg.scala.lemon.task.ResourceUpdateRunnable
import gg.scala.lemon.task.BukkitInstanceUpdateRunnable
import gg.scala.validate.ScalaValidateData
import gg.scala.validate.ScalaValidateUtil
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.entity.EntitySerializer
import net.evilblock.cubed.entity.animation.EntityAnimation
import net.evilblock.cubed.menu.template.MenuTemplate
import net.evilblock.cubed.menu.template.MenuTemplateButton
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.scoreboard.ScoreboardHandler
import net.evilblock.cubed.serialize.BlockVectorAdapter
import net.evilblock.cubed.serialize.ItemStackAdapter
import net.evilblock.cubed.serialize.LocationAdapter
import net.evilblock.cubed.serialize.VectorAdapter
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.AbstractTypeSerializer
import net.evilblock.cubed.store.uuidcache.impl.RedisUUIDCache
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.ClassUtils
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BlockVector
import xyz.mkotb.configapi.ConfigFactory
import java.util.*
import java.util.UUID

class Lemon : ExtendedScalaPlugin()
{

    companion object
    {
        @JvmStatic
        lateinit var instance: Lemon

        @JvmStatic
        var canJoin: Boolean = true
    }

    lateinit var mongoConfig: MongoDBConfigProcessor
    lateinit var settings: SettingsConfigProcessor
    lateinit var languageConfig: LanguageConfigProcessor

    private lateinit var configFactory: ConfigFactory

    lateinit var banana: Banana
    lateinit var credentials: BananaCredentials

    lateinit var serverLayer: RedisStorageLayer<ServerInstance>
    lateinit var localInstance: ServerInstance

    lateinit var lemonWebData: ScalaValidateData
    lateinit var serverStatisticProvider: ServerStatisticProvider

    lateinit var redisConnection: RedisConnection

    val clientAdapters = mutableListOf<PlayerClientAdapter>()

    val init = System.currentTimeMillis()

    override fun enable()
    {
        instance = this

        loadBaseConfigurations()

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

        runAfterDataValidation()
    }

    private fun runAfterDataValidation()
    {
        Serializers.useGsonBuilderThenRebuild { builder ->
            builder.serializeNulls()
                .setLongSerializationPolicy(LongSerializationPolicy.STRING)
                .registerTypeHierarchyAdapter(ItemStack::class.java, ItemStackAdapter())
                .registerTypeHierarchyAdapter(Location::class.java, LocationAdapter())
                .registerTypeHierarchyAdapter(Vector::class.java, VectorAdapter())
                .registerTypeHierarchyAdapter(BlockVector::class.java, BlockVectorAdapter())
                .registerTypeAdapter(Entity::class.java, EntitySerializer)
                .registerTypeAdapter(UUID::class.java, UUIDAdapter)
                .registerTypeAdapter(LemonPlayer::class.java, LemonPlayerAdapter)
                .registerTypeAdapter(EntityAnimation::class.java, AbstractTypeSerializer<EntityAnimation>())
                .registerTypeAdapter(MenuTemplate::class.java, AbstractTypeSerializer<MenuTemplate<*>>())
                .registerTypeAdapter(MenuTemplateButton::class.java, AbstractTypeSerializer<MenuTemplateButton>())
        }

        loadExtraConfigurations()

        loadListeners()
        loadHandlers()
        loadCommands()

        startMessageQueues()
        setupPlayerLookAndFeel()

        Schedulers.async().runRepeating(
            ResourceUpdateRunnable,
            0L, 20L
        )

        Schedulers.async().runRepeating(
            BukkitInstanceUpdateRunnable,
            0L, 100L
        )

        startUuidCacheImplementation()
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

    private fun startMessageQueues()
    {
        LemonOutgoingMessageQueue.start()

        logger.info("Started all outgoing jedis message queues.")
    }

    private fun loadListeners()
    {
        server.pluginManager.registerEvents(PlayerListener, this)
    }

    private fun loadCommands()
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
    }

    private fun setupPlayerLookAndFeel()
    {
        val initialization = System.currentTimeMillis()

        CC.setup(
            toCCColorFormat(lemonWebData.primary),
            toCCColorFormat(lemonWebData.secondary)
        )

        NametagHandler.registerProvider(DefaultNametagProvider())
        NametagHandler.registerProvider(VanishNametagProvider())
        NametagHandler.registerProvider(ModModeNametagProvider())

        ScoreboardHandler.scoreboardOverride = ModModeBoardProvider

        VisibilityHandler.registerAdapter("staff", StaffVisibilityHandler())

        if (settings.disguiseEnabled)
        {
            DisguiseInfoProvider.initialLoad()
            DisguiseProvider.initialLoad()

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
            PlayerCachingExtension.initialLoad()

            logger.info("Now memorizing fundamental player data to your redis server.")
        }

        // Loading all default player colors
        if (settings.playerColorsEnabled)
        {
            PlayerColorHandler.initialLoad()

            logger.info("Loaded default player colors for /colors.")
        }

        // filter through the different client implementations
        // & register the ones which have the plugins enabled
        ClassUtils.getClassesInPackage(
            this, "gg.scala.lemon.adapter.client.impl"
        ).filter {
            server.pluginManager.getPlugin(
                it.getAnnotation(ClientMetadata::class.java).plugin
            ) != null
        }.forEach {
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
            ProtocolLibHook.initialLoad()

            logger.info("Now handling tab-completion through ProtocolLib.")
        }

        serverStatisticProvider = DefaultSparkServerStatisticProvider

        if (server.pluginManager.getPlugin("spark") != null)
        {
            serverStatisticProvider = SparkServerStatisticProvider()

            logger.info("Now utilizing spark for server statistics.")
        }

        logger.info("Finished player qol initialization in ${
            System.currentTimeMillis() - initialization
        }ms.")
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

    private fun loadExtraConfigurations()
    {
        languageConfig = configFactory.fromFile("language", LanguageConfigProcessor::class.java)
        credentials = configFactory.fromFile("redis", BananaCredentials::class.java)
        mongoConfig = configFactory.fromFile("mongodb", MongoDBConfigProcessor::class.java)
    }

    private fun loadHandlers()
    {
        RankHandler.loadRanks()

        CooldownHandler.initialLoad()
        LemonCooldownHandler.initialLoad()

        localInstance = ServerInstance(
            settings.id,
            settings.group
        )

        redisConnection = if (!credentials.authenticate)
        {
            NoAuthRedisConnection(
                credentials.address,
                credentials.port
            )
        } else
        {
            AuthRedisConnection(
                credentials.address,
                credentials.port,
                credentials.password
            )
        }

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

        val builder = RedisStorageBuilder<ServerInstance>()

        builder.setConnection(redisConnection)
        builder.setSection("lemon:heartbeats")
        builder.setType(ServerInstance::class.java)

        serverLayer = builder.build()

        logger.info("Setup redis data-store handling.")
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

            val lemonPlayer = lemonPlayerOptional.orElse(null)!!

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
                Bukkit.getOnlinePlayers().forEach { player ->
                    it.add(player.name)
                }
            }
        }
    }

    override fun disable()
    {
        banana.useResource {
            it.hdel("lemon:heartbeats", settings.id)
            it.close()
        }
    }
}
