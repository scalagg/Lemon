package com.solexgames.lemon

import com.solexgames.daddyshark.commons.constants.DaddySharkConstants
import com.solexgames.daddyshark.commons.logger.BetterConsoleLogger
import com.solexgames.daddyshark.commons.model.ServerInstance
import com.solexgames.daddyshark.commons.platform.DaddySharkPlatform
import com.solexgames.datastore.commons.connection.impl.RedisConnection
import com.solexgames.datastore.commons.connection.impl.redis.AuthRedisConnection
import com.solexgames.datastore.commons.connection.impl.redis.NoAuthRedisConnection
import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import com.solexgames.datastore.commons.logger.ConsoleLogger
import com.solexgames.datastore.commons.storage.impl.RedisStorageBuilder
import com.solexgames.lemon.command.HistoryCommand
import com.solexgames.lemon.command.ShutdownCommand
import com.solexgames.lemon.handler.*
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.player.board.ModModeBoardProvider
import com.solexgames.lemon.player.cached.CachedLemonPlayer
import com.solexgames.lemon.player.nametag.DefaultNametagProvider
import com.solexgames.lemon.player.nametag.ModModeNametagProvider
import com.solexgames.lemon.player.nametag.VanishNametagProvider
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.lemon.processor.LanguageConfigProcessor
import com.solexgames.lemon.processor.MongoDBConfigProcessor
import com.solexgames.lemon.processor.RedisConfigProcessor
import com.solexgames.lemon.processor.SettingsConfigProcessor
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
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.scoreboard.ScoreboardHandler
import net.evilblock.cubed.store.uuidcache.impl.RedisUUIDCache
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.ClassUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import xyz.mkotb.configapi.ConfigFactory
import java.util.function.Consumer

class Lemon: ExtendedJavaPlugin(), DaddySharkPlatform {

    companion object {
        @JvmStatic
        lateinit var instance: Lemon

        @JvmStatic
        var canJoin: Boolean = false
    }

    lateinit var mongoHandler: MongoHandler
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

    private lateinit var consoleLogger: BetterConsoleLogger
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
            "GOLD",
            "YELLOW",
            "discord.gg/solexgames",
            "twitter.com/solexgames",
            "solexgames.com",
            "store.solexgames.com"
        )

        runAfterDataValidation()
    }

    private fun runAfterDataValidation() {
        loadExtraConfigurations()
        loadCosmetics()
        loadListeners()
        loadHandlers()
        loadCommands()

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

    override fun disable() {
        mongoHandler.close()
    }

    private fun loadCommands() {
        val commandManager = CubedCommandManager(this)

//        commandManager.commandCompletions.registerAsyncCompletion("tags") {
//
//        }
        commandManager.commandCompletions.registerAsyncCompletion("ranks") {
            return@registerAsyncCompletion rankHandler.ranks.map { it.value.name }
        }
//        commandManager.commandContexts.registerContext(ServerInstance::class.java) {
//            var server = serverHandler.findServer(it.firstArg)
//
//            if (!server.isPresent) {
//                throw ConditionFailedException("That server does not exist.")
//            }
//            return@registerContext server
//        }
        commandManager.commandContexts.registerContext(Rank::class.java) {
            val rank = rankHandler.findRank(it.firstArg)

            if (!rank.isPresent) {
                throw ConditionFailedException("That rank does not exist.")
            }
            return@registerContext rank.get()
        }

        commandManager.commandContexts.registerContext(LemonPlayer::class.java) {
            val lemonPlayer = playerHandler.findPlayer(it.firstArg)

            if (!lemonPlayer.isPresent) {
                throw ConditionFailedException("Could not find that player.")
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
        registerCommandAction.accept("com.solexgames.lemon.command.conversation")

        logger.info("Loaded command manager")
    }

    private fun loadCosmetics() {
        CC.setup(
            ChatColor.valueOf(lemonWebData.primary).toString(),
            ChatColor.valueOf(lemonWebData.secondary).toString()
        )

        NametagHandler.registerProvider(DefaultNametagProvider())
        NametagHandler.registerProvider(VanishNametagProvider())
        NametagHandler.registerProvider(ModModeNametagProvider())

        ScoreboardHandler.scoreboardOverride = ModModeBoardProvider

//        VisibilityHandler.registerAdapter("Staff", StaffVisiblityHandler())
//        VisibilityHandler.registerOverride("Staff", StaffVisibilityOverrideHandler())
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
        mongoHandler = MongoHandler
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
        consoleLogger = BetterConsoleLogger()

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
