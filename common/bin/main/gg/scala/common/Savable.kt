package gg.scala.common

import java.util.concurrent.CompletableFuture

interface Savable {

    fun save(): CompletableFuture<Void>

}
