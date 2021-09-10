package com.solexgames.lemon

import com.google.gson.LongSerializationPolicy
import com.solexgames.daddyshark.commons.constants.DaddySharkConstants
import com.solexgames.daddyshark.commons.model.ServerInstance
import com.solexgames.daddyshark.commons.platform.DaddySharkPlatform
import com.solexgames.datastore.commons.connection.impl.RedisConnection
import com.solexgames.datastore.commons.connection.impl.redis.AuthRedisConnection
import com.solexgames.datastore.commons.connection.impl.redis.NoAuthRedisConnection
import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import com.solexgames.datastore.commons.logger.ConsoleLogger
import com.solexgames.datastore.commons.storage.impl.RedisStorageBuilder
import com.solexgames.lemon.adapt.LemonPlayerAdapter
import com.solexgames.lemon.adapt.UUIDAdapter
import com.solexgames.lemon.adapt.daddyshark.DaddySharkLogAdapter
import com.solexgames.lemon.handler.*
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.player.board.ModModeBoardProvider
import com.solexgames.lemon.player.cached.CachedLemonPlayer
import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.player.nametag.DefaultNametagProvider
import com.solexgames.lemon.player.nametag.ModModeNametagProvider
import com.solexgames.lemon.player.nametag.VanishNametagProvider
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.lemon.player.visibility.StaffVisibilityHandler
import com.solexgames.lemon.player.visibility.StaffVisibilityOverrideHandler
import com.solexgames.lemon.processor.LanguageConfigProcessor
import com.solexgames.lemon.processor.MongoDBConfigProcessor
import com.solexgames.lemon.processor.RedisConfigProcessor
import com.solexgames.lemon.processor.SettingsConfigProcessor
import com.solexgames.lemon.task.GrantUpdateRunnable
import com.solexgames.lemon.task.daddyshark.BukkitInstanceUpdateRunnable
import com.solexgames.lemon.util.validate.LemonWebData
import com.solexgames.lemon.util.validate.LemonWebStatus
import com.solexgames.redis.JedisBuilder
import com.solexgames.redis.JedisManager
import com.solexgames.redis.JedisSettings
import me.lucko.helper.Schedulers
import me.lucko.helper.plugin.ExtendedJavaPlugin
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.MessageKeys
import net.evilblock.cubed.acf.MessageType
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
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BlockVector
import xyz.mkotb.configapi.ConfigFactory
import java.util.*
import java.util.UUID
import java.util.function.Consumer

class Lemon: ExtendedJavaPlugin(), DaddySharkPlatform {

    companion object {
        @JvmStatic
        lateinit var instance: Lemon

        @JvmStatic
        var canJoin: Boolean = false
    }

    lateinit var mongoHandler: DataStoreHandler
    lateinit var playerHandler: PlayerHandler
    lateinit var rankHandler: RankHandler
    lateinit var grantHandler: GrantHandler
    lateinit var serverHandler: ServerHandler
    lateinit var chatHandler: ChatHandler
    lateinit var punishmentHandler: PunishmentHandler

    lateinit var mongoConfig: MongoDBConfigProcessor
    lateinit var settings: SettingsConfigProcessor
    lateinit var languageConfig: LanguageConfigProcessor

    private lateinit var redisConfig: RedisConfigProcessor
    private lateinit var configFactory: ConfigFactory

    lateinit var jedisManager: JedisManager
    lateinit var jedisSettings: JedisSettings

    lateinit var lemonWebData: LemonWebData

    private lateinit var playerLayer: RedisStorageLayer<CachedLemonPlayer>

    private lateinit var consoleLogger: ConsoleLogger
    private lateinit var localInstance: ServerInstance
    private lateinit var redisConnection: RedisConnection

    override fun enable() {
        instance = this

        loadBaseConfigurations()

//        LemonWebUtil.fetchServerData(settings.serverPassword).whenComplete { webData, throwable ->
////            if (throwable != null || webData == null) {
////                logger.info("Something went wrong during data validation, shutting down... (${throwable?.message})")
////                server.pluginManager.disablePlugin(this)
////
////                return@whenComplete
////            }
////
////            if (webData.status == LemonWebStatus.FAILED) {
////                logger.info("Something went wrong during data validation, shutting down... (${webData.message})")
////                server.pluginManager.disablePlugin(this)
////
////                return@whenComplete
////            }
//
//            logger.info("Passed data validation checks, now loading Lemon with ${webData.serverName}'s data.")
//
//            lemonWebData = webData
//
//            runAfterDataValidation()
//        }

        lemonWebData = LemonWebData(
            LemonWebStatus.SUCCESS,
            "DEV",
            "SolexGames",
            "GREEN",
            "YELLOW",
            "discord.gg/solexgames",
            "twitter.com/solexgames",
            "solexgames.com",
            "store.solexgames.com"
        )

        runAfterDataValidation()
    }

    private fun runAfterDataValidation() {
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
        setupPlayerLookAndFeel()
        loadListeners()
        loadHandlers()
        loadCommands()

        Schedulers.async().runRepeating(GrantUpdateRunnable(), 0L, 20L)

        Schedulers.async().runRepeating(
            BukkitInstanceUpdateRunnable(this),
            0L, DaddySharkConstants.UPDATE_DELAY_MILLI
        )

        server.consoleSender.sendMessage(
            "${CC.PRI}Lemon${CC.SEC} version ${CC.PRI}${description.version}${CC.SEC} has loaded and the server will be joinable in ${CC.GREEN}3 seconds${CC.SEC}."
        )

        Cubed.instance.uuidCache = RedisUUIDCache(jedisManager)

        Schedulers.sync().runLater({
            canJoin = true
        }, 60L)
    }

    private fun loadCommands() {
        val commandManager = CubedCommandManager(this)

        listOf<MessageType>(MessageType.HELP, MessageType.ERROR, MessageType.INFO, MessageType.SYNTAX).forEach {
            commandManager.getFormat(it).setColor(2, ChatColor.valueOf(lemonWebData.secondary))
            commandManager.getFormat(it).setColor(1, ChatColor.valueOf(lemonWebData.primary))
        }

        commandManager.commandCompletions.registerAsyncCompletion("ranks") {
            return@registerAsyncCompletion rankHandler.ranks.map { it.value.name }
        }

        commandManager.commandCompletions.registerAsyncCompletion("ranks") {
            return@registerAsyncCompletion rankHandler.ranks.map { it.value.name }
        }

        commandManager.commandContexts.registerContext(Rank::class.java) {
            val rank = rankHandler.findRank(it.firstArg)

            if (!rank.isPresent) {
                throw ConditionFailedException("Could not find a rank by the name: ${CC.YELLOW}${it.firstArg}${CC.RED}.")
            }

            return@registerContext rank.get()
        }

        commandManager.commandContexts.registerContext(Channel::class.java) {
            return@registerContext chatHandler.findChannel(it.firstArg)
                ?: throw ConditionFailedException("Could not find a channel by the name: ${CC.YELLOW}${it.firstArg}${CC.RED}.")
        }

        commandManager.commandContexts.registerContext(LemonPlayer::class.java) {
            val lemonPlayer = playerHandler.findPlayer(it.firstArg)

            if (!lemonPlayer.isPresent) {
                throw ConditionFailedException("Could not find a player by the name: ${CC.YELLOW}${it.firstArg}${CC.RED}.")
            }

            return@registerContext lemonPlayer.get()
        }

        commandManager.commandCompletions.registerAsyncCompletion("players") {
            val list = mutableListOf<String>()

            Bukkit.getOnlinePlayers().forEach {
                list.add(it.name)
            }

            return@registerAsyncCompletion list
        }

        commandManager.commandCompletions.registerAsyncCompletion("players-uv") {
            val list = mutableListOf<String>()

            Bukkit.getOnlinePlayers().forEach {
                if (!it.hasMetadata("vanished")) {
                    list.add(it.name)
                }
            }

            return@registerAsyncCompletion list
        }

        val registerCommandAction = Consumer<String> {
            ClassUtils.getClassesInPackage(this, it).forEach { clazz ->
                commandManager.registerCommand(clazz.newInstance() as BaseCommand)
            }
        }

        registerCommandAction.accept("com.solexgames.lemon.command")

        logger.info("Loaded command manager")
    }

    private fun setupPlayerLookAndFeel() {
        CC.setup(
            toCCColorFormat(lemonWebData.primary),
            toCCColorFormat(lemonWebData.secondary)
        )

        NametagHandler.registerProvider(DefaultNametagProvider())
        NametagHandler.registerProvider(VanishNametagProvider())
        NametagHandler.registerProvider(ModModeNametagProvider())

        ScoreboardHandler.scoreboardOverride = ModModeBoardProvider

        VisibilityHandler.registerAdapter("Staff", StaffVisibilityHandler())
        VisibilityHandler.registerOverride("Staff", StaffVisibilityOverrideHandler())
    }

    private fun toCCColorFormat(string: String): String {
        return ChatColor.valueOf(string).toString()
    }

    private fun loadListeners() {
        ClassUtils.getClassesInPackage(this, "com.solexgames.lemon.listener").forEach {
            val listener = it.newInstance() as Listener

            server.pluginManager.registerEvents(listener, this)
        }
    }

    private fun loadBaseConfigurations() {
        configFactory = ConfigFactory.newFactory(this)

        settings = configFactory.fromFile("settings", SettingsConfigProcessor::class.java)
    }

    private fun loadExtraConfigurations() {
        languageConfig = configFactory.fromFile("language", LanguageConfigProcessor::class.java)
        redisConfig = configFactory.fromFile("redis", RedisConfigProcessor::class.java)
        mongoConfig = configFactory.fromFile("mongodb", MongoDBConfigProcessor::class.java)
    }

    private fun loadHandlers() {
        mongoHandler = DataStoreHandler
        playerHandler = PlayerHandler

        rankHandler = RankHandler
        rankHandler.loadAllRanks()

        grantHandler = GrantHandler
        serverHandler = ServerHandler
        chatHandler = ChatHandler
        punishmentHandler = PunishmentHandler

        localInstance = ServerInstance(
            settings.id,
            settings.group
        )

        redisConnection = if (!redisConfig.authentication) {
            NoAuthRedisConnection(
                redisConfig.address,
                redisConfig.port
            )
        } else {
            AuthRedisConnection(
                redisConfig.address,
                redisConfig.port,
                redisConfig.password
            )
        }

        val layerBuilder = RedisStorageBuilder<CachedLemonPlayer>()

        layerBuilder.setType(CachedLemonPlayer::class.java)
        layerBuilder.setSection("lemon:players")
        layerBuilder.setConnection(redisConnection)

        playerLayer = layerBuilder.build()
        consoleLogger = DaddySharkLogAdapter()

        jedisSettings = JedisSettings(
            redisConfig.address,
            redisConfig.port,
            redisConfig.authentication,
            redisConfig.password
        )

        jedisManager = JedisBuilder()
            .withSettings(jedisSettings)
            .withChannel("lemon:spigot")
            .withHandler(RedisHandler).build()

        setupDataStore()
    }

    override var layer: RedisStorageLayer<ServerInstance>? = null

    override fun getConsoleLogger(): ConsoleLogger {
        return consoleLogger
    }

    override fun getLocalServerInstance(): ServerInstance {
        return localInstance
    }

    override fun getRedisConnection(): RedisConnection {
        return redisConnection
    }
}
