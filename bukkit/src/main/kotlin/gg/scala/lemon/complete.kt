package gg.scala.lemon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 10/11/2021
 */
fun main()
{
    CompletableFuture.supplyAsync {
        println("hi")
    }.thenRun {
        println("printed after hi")
    }

    CompletableFuture.supplyAsync {
        "hi"
    }.thenAccept {
        println("$it <-- this is hi")
    }

    CompletableFuture.supplyAsync {
        println("hi")
//
//        throw RuntimeException(":(")
    }.whenComplete { _, u ->
        println("printed after hi")

        // u is not null as we threw a runtime exception after hi
        u?.printStackTrace()
    }

//    async(Dispatchers.IO) {
//        println("now ran async")
//    }

    println("should print before \"now ran async\"")
}
