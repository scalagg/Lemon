package com.solexgames.lemon.model

import java.util.concurrent.CompletableFuture

interface Saveable {

    fun save(): CompletableFuture<Void>

}