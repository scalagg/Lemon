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
import com.solexgames.lemon.handler.*
import com.solexgames.lemon.listener.PlayerListener
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.player.cached.CachedLemonPlayer
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.lemon.processor.MongoDBConfigProcessor
import com.solexgames.lemon.processor.RedisConfigProcessor
import com.solexgames.lemon.processor.SettingsConfigProcessor
import com.solexgames.lemon.task.impl.daddyshark.BukkitInstanceUpdateRunnable
import com.solexgames.redis.JedisBuilder
import com.solexgames.redis.JedisManager
import com.solexgames.redis.JedisSettings
import me.lucko.helper.Schedulers
import me.lucko.helper.plugin.ExtendedJavaPlugin
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.command.manager.CubedCommandManager
import net.evilblock.cubed.util.CC
import xyz.mkotb.configapi.ConfigFactory

class Lemon : ExtendedJavaPlugin(), DaddySharkPlatform {

    companion object {
        @JvmStatic
        lateinit var instance: Lemon
    }

    lateinit var mongoHandler: MongoHandler
    lateinit var playerHandler: PlayerHandler
    lateinit var rankHandler: RankHandler
    lateinit var grantHandler: GrantHandler
    lateinit var serverHandler: ServerHandler

    lateinit var redisConfig: RedisConfigProcessor
    lateinit var mongoConfig: MongoDBConfigProcessor
    lateinit var settings: SettingsConfigProcessor

    lateinit var configFactory: ConfigFactory

    lateinit var jedisManager: JedisManager
    lateinit var jedisSettings: JedisSettings

    private lateinit var playerLayer: RedisStorageLayer<CachedLemonPlayer>

    private lateinit var consoleLogger: BetterConsoleLogger
    private lateinit var localInstance: ServerInstance
    private lateinit var redisConnection: RedisConnection

    override fun enable() {
        instance = this

        loadConfigurations()
        loadCosmetics()
        loadListeners()
        loadHandlers()
        loadCommands()

        Schedulers.async().runRepeating(
            BukkitInstanceUpdateRunnable(this),
            0L, DaddySharkConstants.UPDATE_DELAY_MILLI
        )

        logger.info("Loaded Lemon")
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
        // prefix commandContext
        commandManager.commandContexts.registerContext(LemonPlayer::class.java) {
            val lemonPlayer = playerHandler.findPlayer(it.firstArg)

            if (!lemonPlayer.isPresent) {
                throw ConditionFailedException("Could not find that player.")
            }
            return@registerContext lemonPlayer.get()
        }

        logger.info("Loaded command manager")
    }

    private fun loadCosmetics() {
        CC.setup(
            settings.primaryColor.toString(),
            settings.secondaryColor.toString()
        )

//        NameTagHandler.registerProvider(DefaultNametagProvider())
//        NameTagHandler.registerProvider(VanishNametagProvider())
//        NameTagHandler.registerProvider(StaffModeNametagProvider())
//
//        VisibilityHandler.registerAdapter("Staff", StaffVisibilityHandler())
//        VisibilityHandler.registerOverride("Staff", StaffVisibilityOverrideHandler())

        logger.info("Loaded cosmetics")
    }

    private fun loadListeners() {
        server.pluginManager.registerEvents(PlayerListener(this), this)

        logger.info("Loaded listeners")
    }

    private fun loadConfigurations() {
        configFactory = ConfigFactory.newFactory(this)

        redisConfig = configFactory.fromFile("redis", RedisConfigProcessor.javaClass)
        mongoConfig = configFactory.fromFile("mongodb", MongoDBConfigProcessor.javaClass)
        settings = configFactory.fromFile("settings", SettingsConfigProcessor.javaClass)

        logger.info("Loaded configurations")
    }

    private fun loadHandlers() {
        mongoHandler = MongoHandler
        playerHandler = PlayerHandler
        rankHandler = RankHandler
        grantHandler = GrantHandler
        serverHandler = ServerHandler

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

        logger.info("Loaded handlers")
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
