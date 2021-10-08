package gg.scala.lemon.adapt.daddyshark

import com.solexgames.datastore.commons.logger.ConsoleLogger

/**
 * @author GrowlyX
 * @since 9/9/2021
 */
object DaddySharkLogAdapter : ConsoleLogger() {

    override fun log(p0: String?) {
        println("[Lemon] [Internal] $p0")
    }
}
