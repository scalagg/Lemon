package gg.scala.lemon.scope

data class ServerScope(
    val group: String,
    val individual: MutableList<String> = mutableListOf()
)
