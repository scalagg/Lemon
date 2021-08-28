package com.solexgames.lemon.util.type

interface Permissible<T> {

    fun getPermission(): String?

    fun hasPermission(t: T): Boolean

}
