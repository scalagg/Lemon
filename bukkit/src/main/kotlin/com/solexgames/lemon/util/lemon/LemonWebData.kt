package com.solexgames.lemon.util.lemon

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
data class LemonWebData(
    val status: LemonWebStatus,
    val message: String,

    val serverName: String,

    val primary: String,
    val secondary: String,

    val discord: String,
    val twitter: String,
    val domain: String,
    val store: String,
)
