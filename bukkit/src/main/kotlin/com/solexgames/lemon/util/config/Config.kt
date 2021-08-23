package com.solexgames.lemon.util.config

import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

class Config(name: String, plugin: JavaPlugin) {

    protected var wasCreated = false
    var configFile: File
    var config: FileConfiguration

    init {
        configFile = File(plugin.dataFolder, "$name.yml")

        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            try {
                plugin.saveResource(configFile.getName(), false)
            } catch (ex: IllegalArgumentException) {
                try {
                    configFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            wasCreated = true
        }

        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun saveConfig() {
        try {
            config.save(configFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    operator fun get(path: String): Any {
        return config[path]
    }

    fun getDouble(path: String): Double {
        return if (config.contains(path)) {
            config.getDouble(path)
        } else 0.0
    }

    fun getFloat(path: String): Float {
        return if (config.contains(path)) {
            if (config[path] is Float) config[path] as Float else 0.0f
        } else 0.0f
    }

    fun getInt(path: String): Int {
        return if (config.contains(path)) {
            config.getInt(path)
        } else 0
    }

    fun getBoolean(path: String): Boolean {
        return config.contains(path) && config.getBoolean(path)
    }

    operator fun contains(path: String): Boolean {
        return config.contains(path)
    }

    fun getString(path: String): String {
        return config.getString(path)
    }

    fun getString(path: String, callback: String): String {
        return getString(path, callback, false)
    }

    fun getString(path: String, callback: String, colorize: Boolean): String {
        if (!config.contains(path)) {
            return callback
        }
        return if (colorize) {
            ChatColor.translateAlternateColorCodes('&', config.getString(path))
        } else config.getString(path)
    }

    fun getReversedStringList(path: String): List<String> {
        val list = getStringList(path)
        if (list != null) {
            val size = list.size
            val toReturn: MutableList<String> = ArrayList()
            for (i in size - 1 downTo 0) {
                toReturn.add(list[i])
            }
            return toReturn
        }
        return ArrayList()
    }

    fun getStringList(path: String): List<String> {
        if (config.contains(path)) {
            val strings = ArrayList<String>()
            for (string in config.getStringList(path)) {
                strings.add(ChatColor.translateAlternateColorCodes('&', string))
            }
            return strings
        }
        return ArrayList()
    }

    fun getStringListOrDefault(path: String, toReturn: List<String>): List<String> {
        if (config.contains(path)) {
            val strings = ArrayList<String>()
            for (string in config.getStringList(path)) {
                strings.add(ChatColor.translateAlternateColorCodes('&', string))
            }
            return strings
        }
        return toReturn
    }
}