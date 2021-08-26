package com.solexgames.lemon.type

interface Prefixable {

    fun getPrefix(): String

    fun shouldCheckForPrefix(): Boolean

    fun isPrefixed(message: String): Boolean

}
