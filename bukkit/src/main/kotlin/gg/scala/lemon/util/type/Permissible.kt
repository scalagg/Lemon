package gg.scala.lemon.util.type

interface Permissible<T> {

    fun getPermission(): String?

    fun hasPermission(t: T): Boolean

}
