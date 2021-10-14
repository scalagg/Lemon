package gg.scala.common

interface Permissible<T> {

    fun getPermission(): String?

    fun hasPermission(t: T): Boolean

}
