package gg.scala.common.metadata

/**
 * @author GrowlyX
 * @since 8/27/2024
 */
data class NetworkProperties(
    val forbiddenCommands: MutableSet<String> = mutableSetOf("plugins", "pl"),
    var rankPrefixInNametags: Boolean = true,
    var tablistSortingEnabled: Boolean = true,
)
