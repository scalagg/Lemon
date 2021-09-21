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
import gg.scala.lemon.adapt.daddyshark.DaddySharkLogAdapter
import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.board.ModModeBoardProvider
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.nametag.DefaultNametagProvider
import gg.scala.lemon.player.nametag.ModModeNametagProvider
import gg.scala.lemon.player.nametag.VanishNametagProvider
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.player.visibility.StaffVisibilityHandler
import gg.scala.lemon.player.visibility.StaffVisibilityOverrideHandler
import gg.scala.lemon.processor.LanguageConfigProcessor
import gg.scala.lemon.processor.MongoDBConfigProcessor
import gg.scala.lemon.processor.SettingsConfigProcessor
import gg.scala.lemon.task.ResourceUpdateRunnable
import gg.scala.lemon.task.daddyshark.BukkitInstanceUpdateRunnable
import gg.scala.lemon.util.validate.LemonWebData
import gg.scala.lemon.util.validate.LemonWebStatus
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
import net.evilblock.cubed.util.bukkit.selection.impl.EntityInteractionHandler
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

class Lemon: ExtendedJavaPlugin(), DaddySharkPlatform {

    companion object {
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

//    private lateinit var playerLayer: RedisStorageLayer<CachedLemonPlayer>

    private lateinit var consoleLogger: ConsoleLogger
    private lateinit var localInstance: ServerInstance
    private lateinit var redisConnection: RedisConnection

    override fun enable() {
        instance = this

        loadBaseConfigurations()

//        val webData = LemonWebUtil.fetchServerData(settings.serverPassword)
//
//        if (webData == null) {
//            consoleLogger.log("Something went wrong during data validation, shutting down... (webData=null)")
//            server.pluginManager.disablePlugin(this)
//
//            return
//        }
//
//        if (webData.result == LemonWebStatus.FAILED) {
//            consoleLogger.log("Your server password's incorrect. (${webData.message})")
//            server.pluginManager.disablePlugin(this)
//
//            return
//        }

        consoleLogger.log(
            "Passed data validation checks, now loading Lemon with ${"SolexGames"}'s information..."
        ); lemonWebData = LemonWebData(
            LemonWebStatus.SUCCESS,
            "",
            "SolexGames",
            "GREEN",
            "YELLOW",
            "discord.gg/solexgames",
            "SolexGamesCOM",
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

        server.scheduler.runTaskTimerAsynchronously(this, ResourceUpdateRunnable(), 0L, 20L)

        Schedulers.async().runRepeating(
            BukkitInstanceUpdateRunnable(this),
            0L, DaddySharkConstants.UPDATE_DELAY_MILLI
        )

        server.consoleSender.sendMessage(
            "${CC.PRI}Lemon${CC.SEC} version ${CC.PRI}${description.version}${CC.SEC} has loaded. Player will be able to join in ${CC.GREEN}3 seconds${CC.SEC}."
        )

        Cubed.instance.uuidCache = RedisUUIDCache(banana)

        Schedulers.sync().runLater({
            canJoin = true
        }, 60L)
    }

    private fun loadCommands() {
        val commandManager = CubedCommandManager(
            plugin = this,
            primary = ChatColor.valueOf(lemonWebData.primary),
            secondary = ChatColor.valueOf(lemonWebData.secondary)
        )

        registerCompletionsAndContexts(commandManager)
        registerCommandsInPackage(commandManager, "gg.scala.lemon.command")

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
        ClassUtils.getClassesInPackage(this, "gg.scala.lemon.listener").forEach {
            val listener = it.newInstance() as Listener

            server.pluginManager.registerEvents(listener, this)
        }
    }

    private fun loadBaseConfigurations() {
        consoleLogger = DaddySharkLogAdapter()
        configFactory = ConfigFactory.newFactory(this)

        settings = configFactory.fromFile("settings", SettingsConfigProcessor::class.java)
    }

    private fun loadExtraConfigurations() {
        languageConfig = configFactory.fromFile("language", LanguageConfigProcessor::class.java)
        credentials = configFactory.fromFile("redis", BananaCredentials::class.java)
        mongoConfig = configFactory.fromFile("mongodb", MongoDBConfigProcessor::class.java)
    }

    private fun loadHandlers() {
        RankHandler.loadRanks()

        localInstance = ServerInstance(
            settings.id,
            settings.group
        )

        redisConnection = if (!credentials.authenticate) {
            NoAuthRedisConnection(
                credentials.address,
                credentials.port
            )
        } else {
            AuthRedisConnection(
                credentials.address,
                credentials.port,
                credentials.password
            )
        }

//        val layerBuilder = RedisStorageBuilder<CachedLemonPlayer>()
//
//        layerBuilder.setType(CachedLemonPlayer::class.java)
//        layerBuilder.setSection("lemon:players")
//        layerBuilder.setConnection(redisConnection)
//
//        playerLayer = layerBuilder.build()

        banana = BananaBuilder()
            .options(
                BananaOptions(
                    channel = "lemon:spigot",
                    async = true,
                    gson = Serializers.gson
                )
            )
            .credentials(
                credentials
            )
            .build()

        setupDataStore()
    }

    fun registerCommandsInPackage(commandManager: CubedCommandManager, commandPackage: String) {
        ClassUtils.getClassesInPackage(commandManager.plugin, commandPackage).forEach { clazz ->
            commandManager.registerCommand(clazz.newInstance() as BaseCommand)
        }
    }

    fun registerCompletionsAndContexts(commandManager: CubedCommandManager) {
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
            val lemonPlayer = PlayerHandler.findPlayer(firstArgument)

            if (!lemonPlayer.isPresent) {
                throw ConditionFailedException("No player matching ${CC.YELLOW}$firstArgument${CC.RED} could be found.")
            }

            return@registerContext lemonPlayer.get()
        }

        commandManager.commandCompletions.registerAsyncCompletion("all-players") {
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
