package gg.scala.lemon.scope

/**
 * @author GrowlyX
 * @since 10/20/2022
 */
data class ServerScope(
    val group: String,
    val individual: MutableList<String> = mutableListOf()
)
