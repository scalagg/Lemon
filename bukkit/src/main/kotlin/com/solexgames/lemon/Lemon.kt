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
import com.solexgames.lemon.handler.*
import com.solexgames.lemon.listener.PlayerListener
import com.solexgames.lemon.processor.MongoDBConfigProcessor
import com.solexgames.lemon.processor.RedisConfigProcessor
import com.solexgames.lemon.processor.SettingsConfigProcessor
import com.solexgames.lemon.task.impl.daddyshark.BukkitInstanceUpdateRunnable
import com.solexgames.redis.JedisBuilder
import com.solexgames.redis.JedisManager
import com.solexgames.redis.JedisSettings
import me.lucko.helper.Schedulers
import org.bukkit.plugin.java.JavaPlugin
import xyz.mkotb.configapi.ConfigFactory

class Lemon : JavaPlugin(), DaddySharkPlatform {

    companion object {
        @JvmStatic
        lateinit var instance: Lemon
    }

    lateinit var mongoHandler: MongoHandler
    lateinit var playerHandler: PlayerHandler
    lateinit var rankHandler: RankHandler
    lateinit var shutdownHandler: ShutdownHandler
    lateinit var grantHandler: GrantHandler

    lateinit var redisConfig: RedisConfigProcessor
    lateinit var mongoConfig: MongoDBConfigProcessor
    lateinit var settings: SettingsConfigProcessor

    lateinit var configFactory: ConfigFactory

    lateinit var jedisManager: JedisManager
    lateinit var jedisSettings: JedisSettings

    private lateinit var consoleLogger: BetterConsoleLogger
    private lateinit var localInstance: ServerInstance
    private lateinit var redisConnection: RedisConnection

    override fun onEnable() {
        instance = this

        configFactory = ConfigFactory.newFactory(this)

        redisConfig = configFactory.fromFile("redis", RedisConfigProcessor.javaClass)
        mongoConfig = configFactory.fromFile("mongodb", MongoDBConfigProcessor.javaClass)
        settings = configFactory.fromFile("settings", SettingsConfigProcessor.javaClass)

        mongoHandler = MongoHandler
        playerHandler = PlayerHandler
        rankHandler = RankHandler
        shutdownHandler = ShutdownHandler
        grantHandler = GrantHandler

        setupRedisHandler()

        Schedulers.async().runRepeating(
            BukkitInstanceUpdateRunnable(this),
            0L, DaddySharkConstants.UPDATE_DELAY_MILLI
        )

        val listener = PlayerListener(this)
        listener.registerHelperEvents()

        server.pluginManager.registerEvents(listener, this)
    }

    private fun setupRedisHandler() {
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

    override fun onDisable() {
        mongoHandler.close()
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
