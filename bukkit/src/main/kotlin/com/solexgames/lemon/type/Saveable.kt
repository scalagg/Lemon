package com.solexgames.lemon.type

import java.util.concurrent.CompletableFuture

interface Saveable {

    fun save(): CompletableFuture<Void>

}
