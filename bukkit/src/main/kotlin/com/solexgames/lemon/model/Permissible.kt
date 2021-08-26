package com.solexgames.lemon.model

interface Permissible<T> {

    fun getPermission(): String

    fun hasPermission(t: T): Boolean

}
