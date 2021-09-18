package gg.scala.lemon.util.type

import java.util.concurrent.CompletableFuture

interface Savable {

    fun save(): CompletableFuture<Void>

}
