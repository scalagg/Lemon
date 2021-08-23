package com.solexgames.lemon

import com.solexgames.lemon.handler.MongoHandler
import com.solexgames.lemon.handler.PlayerHandler
import com.solexgames.lemon.handler.RankHandler
import com.solexgames.lemon.handler.ShutdownHandler
import com.solexgames.lemon.util.config.Config
import net.evilblock.cubed.util.Color
import org.bukkit.plugin.java.JavaPlugin

class Lemon : JavaPlugin() {

    companion object {
        @JvmStatic lateinit var instance: Lemon
    }

    lateinit var serverName: String

    lateinit var mongoHandler: MongoHandler
    lateinit var playerHandler: PlayerHandler
    lateinit var rankHandler: RankHandler
    lateinit var shutdownHandler: ShutdownHandler

    lateinit var databaseConfig: Config

    override fun onEnable() {
        instance = this

        config.options().copyDefaults()
        saveDefaultConfig()

        databaseConfig = Config("database", this)

        mongoHandler = MongoHandler
        playerHandler = PlayerHandler
        rankHandler = RankHandler
        shutdownHandler = ShutdownHandler
    }

    override fun onDisable() {
        mongoHandler.close()
    }

    fun logConsole(message: String) {
        server.consoleSender.sendMessage(Color.translate(message))
    }
}