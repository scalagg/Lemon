package gg.scala.common.metadata

import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 8/27/2024
 */
class NetworkMetadata(
    var serverName: String = "",
    var primary: String = ChatColor.GOLD.name,
    var secondary: String = ChatColor.YELLOW.name,
    var discord: String = "",
    var twitter: String = "",
    var domain: String = "",
    var store: String = "",
    var properties: NetworkProperties? = null,
    var language: LanguageProperties? = null,
    var initialSaveComplete: Boolean = false
)
{
    fun language() = language ?: let {
        language = LanguageProperties()
        return@let language!!
    }

    fun properties() = properties ?: let {
        properties = NetworkProperties()
        return@let properties!!
    }
}
