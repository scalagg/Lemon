package com.solexgames.lemon.model

import java.util.concurrent.CompletableFuture

interface Loadable<T> {

    fun load(future: CompletableFuture<T>)
    
}