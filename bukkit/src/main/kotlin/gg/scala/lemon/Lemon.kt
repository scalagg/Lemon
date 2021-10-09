package gg.scala.lemon

import com.google.gson.LongSerializationPolicy
import com.solexgames.daddyshark.commons.constants.DaddySharkConstants
import com.solexgames.daddyshark.commons.model.ServerInstance
import com.solexgames.daddyshark.commons.platform.DaddySharkPlatform
import com.solexgames.datastore.commons.connection.impl.RedisConnection
import com.solexgames.datastore.commons.connection.impl.redis.AuthRedisConnection
import com.solexgames.datastore.commons.connection.impl.redis.NoAuthRedisConnection
import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import com.solexgames.datastore.commons.logger.ConsoleLogger
import gg.scala.banana.Banana
import gg.scala.banana.BananaBuilder
import gg.scala.banana.credentials.BananaCredentials
import gg.scala.banana.options.BananaOptions
import gg.scala.lemon.adapt.LemonPlayerAdapter
import gg.scala.lemon.adapt.UUIDAdapter
import gg.scala.lemon.adapt.client.PlayerClientAdapter
import gg.scala.lemon.adapt.daddyshark.DaddySharkLogAdapter
import gg.scala.lemon.adapt.statistic.ServerStatisticProvider
import gg.scala.lemon.adapt.statistic.impl.DefaultSparkServerStatisticProvider
import gg.scala.lemon.adapt.statistic.impl.SparkServerStatisticProvider
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
import gg.scala.lemon.player.nametag.DefaultNametagProvider
import gg.scala.lemon.player.nametag.ModModeNametagProvider
import gg.scala.lemon.player.nametag.VanishNametagProvider
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.visibility.StaffVisibilityHandler
import gg.scala.lemon.processor.LanguageConfigProcessor
import gg.scala.lemon.processor.MongoDBConfigProcessor
import gg.scala.lemon.processor.SettingsConfigProcessor
import gg.scala.lemon.task.ResourceUpdateRunnable
import gg.scala.lemon.task.daddyshark.BukkitInstanceUpdateRunnable
import gg.scala.lemon.util.LemonWebUtil
import gg.scala.lemon.util.validate.LemonWebData
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.plugin.ExtendedJavaPlugin
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.entity.EntitySerializer
import net.evilblock.cubed.entity.animation.EntityAnimation
import net.evilblock.cubed.menu.template.MenuTemplate
import net.evilblock.cubed.menu.template.MenuTemplateButton
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.nametag.update.NametagUpdateEvent
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
import net.evilblock.cubed.util.bukkit.selection.impl.EntityInteractionHandler
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

class Lemon : ExtendedJavaPlugin(), DaddySharkPlatform
{

    companion object
    {
        @JvmStatic
        lateinit var instance: Lemon

        @JvmStatic
        var canJoin: Boolean = false
    }

    lateinit var mongoConfig: MongoDBConfigProcessor
    lateinit var settings: SettingsConfigProcessor
    lateinit var languageConfig: LanguageConfigProcessor

    private lateinit var configFactory: ConfigFactory

    lateinit var banana: Banana
    lateinit var credentials: BananaCredentials

    lateinit var lemonWebData: LemonWebData
    lateinit var entityInteractionHandler: EntityInteractionHandler
    lateinit var serverStatisticProvider: ServerStatisticProvider

    private lateinit var consoleLogger: ConsoleLogger
    private lateinit var localInstance: ServerInstance
    private lateinit var redisConnection: RedisConnection

    val clientAdapters = mutableListOf<PlayerClientAdapter>()


    override fun enable()
    {
        instance = this

        loadBaseConfigurations()

        val webData = LemonWebUtil.fetchServerData(
            settings.serverPassword
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

        setupPlayerLookAndFeel()

        server.scheduler.runTaskTimerAsynchronously(this, ResourceUpdateRunnable(), 0L, 20L)

        Schedulers.async().runRepeating(
            BukkitInstanceUpdateRunnable(this),
            0L, DaddySharkConstants.UPDATE_DELAY_MILLI
        )

//        Schedulers.sync().runRepeating(Runnable { System.gc() }, 0L, 100L)

        server.consoleSender.sendMessage(
            "${CC.PRI}Lemon${CC.SEC} version ${CC.PRI}${description.version}${CC.SEC} has loaded. Players will be able to join in ${CC.GREEN}3 seconds${CC.SEC}."
        )

        Cubed.instance.uuidCache = RedisUUIDCache(banana)

        Schedulers.sync().runLater({ canJoin = true }, 60L)
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
    }

    private fun setupPlayerLookAndFeel()
    {
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
        }

        if (settings.logDataToFile)
        {
            ChatAsyncFileLogger.start()
            CommandAsyncFileLogger.start()

            logger.info("Started log queue for chat & commands")
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

        Events.subscribe(NametagUpdateEvent::class.java).handler {
            val player = it.player

            clientAdapters.forEach { adapter ->
                if (player.hasMetadata("mod-mode"))
                {
                    adapter.updateNametag(
                        player, listOf(
                            player.playerListName,
                            "${CC.GRAY}[Mod Mode]"
                        )
                    )
                    return@forEach
                }

                if (player.hasMetadata("vanished"))
                {
                    adapter.updateNametag(
                        player, listOf(
                            player.playerListName,
                            "${CC.GRAY}[Vanished]"
                        )
                    )
                    return@forEach
                }

                adapter.resetNametag(player)
            }
        }

        // filter through the different client implementations
        // & register the ones which have the plugins enabled
        ClassUtils.getClassesInPackage(
            this, "gg.scala.lemon.adapt.client"
        ).filter {
            Bukkit.getPluginManager().plugins.firstOrNull { plugin ->
                plugin.name.contains(it.simpleName.replace("Adapter", ""))
            } != null
        }.forEach {
            val clientAdapter = it.newInstance() as PlayerClientAdapter
            clientAdapters.add(clientAdapter)

            getConsoleLogger().log(
                "${clientAdapter.getClientName()} implementation has been enabled."
            )
        }

        serverStatisticProvider = DefaultSparkServerStatisticProvider

        if (Bukkit.getPluginManager().getPlugin("spark") != null)
        {
            serverStatisticProvider = SparkServerStatisticProvider()

            logger.info("Now utilizing spark for server statistics.")
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
        consoleLogger = DaddySharkLogAdapter
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

        setupDataStore()
    }

    fun registerCommandsInPackage(
        commandManager: CubedCommandManager,
        commandPackage: String
    )
    {
        ClassUtils.getClassesInPackage(
            commandManager.plugin, commandPackage
        ).forEach { clazz ->
            try
            {
                // kotlin `objects` have the INSTANCE field set as
                // its instance during runtime.

                // filtering through fields so it returns a Field? instead of throwing an error
                val instance = clazz.fields.firstOrNull {
                    it.name.equals("INSTANCE")
                }

                if (instance != null)
                {
                    val instanceObject = instance.get(null)

                    commandManager.registerCommand(
                        instanceObject as BaseCommand
                    )
                } else
                {
                    commandManager.registerCommand(
                        clazz.newInstance() as BaseCommand
                    )
                }
            } catch (e: Exception)
            {
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

            if (!VisibilityHandler.treatAsOnline(lemonPlayer.bukkitPlayer!!, it.player))
            {
                throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
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

    override var layer: RedisStorageLayer<ServerInstance>? = null

    override fun getConsoleLogger(): ConsoleLogger
    {
        return consoleLogger
    }

    override fun getLocalServerInstance(): ServerInstance
    {
        return localInstance
    }

    override fun getRedisConnection(): RedisConnection
    {
        return redisConnection
    }
}
