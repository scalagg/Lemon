package com.solexgames.lemon.type

import java.util.concurrent.CompletableFuture

interface Loadable<T> {

    fun load(future: CompletableFuture<T>)

}
