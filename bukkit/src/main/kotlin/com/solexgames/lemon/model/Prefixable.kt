package com.solexgames.lemon.model

interface Prefixable {

    fun getPrefix(): String

    fun shouldCheckForPrefix(): Boolean

    fun isPrefixed(message: String): Boolean

}
