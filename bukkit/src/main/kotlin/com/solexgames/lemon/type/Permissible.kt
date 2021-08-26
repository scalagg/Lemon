package com.solexgames.lemon.type

interface Permissible<T> {

    fun getPermission(): String

    fun hasPermission(t: T): Boolean

}
