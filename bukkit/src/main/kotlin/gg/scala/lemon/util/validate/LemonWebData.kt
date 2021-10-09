package gg.scala.lemon.util.validate

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
data class LemonWebData(
    val serverName: String,

    val primary: String,
    val secondary: String,

    val discord: String,
    val twitter: String,
    val domain: String,
    val store: String,
)
