package com.solexgames.lemon.util.type

import java.util.concurrent.CompletableFuture

interface Saveable {

    fun save(): CompletableFuture<Void>

}
