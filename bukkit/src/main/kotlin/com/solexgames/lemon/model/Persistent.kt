package com.solexgames.lemon.model

import java.util.concurrent.CompletableFuture

interface Persistent<T> {

    fun load(future: CompletableFuture<T>)

    fun save(): CompletableFuture<Void>

}
