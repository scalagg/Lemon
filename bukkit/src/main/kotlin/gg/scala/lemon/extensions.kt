package gg.scala.lemon

import gg.scala.lemon.processor.SettingsConfigProcessor

/**
 * @author GrowlyX
 * @since 7/23/2022
 */
fun minequest() = Lemon.instance.lemonWebData.serverName == "Minequest"
fun config() = Lemon.instance.config<SettingsConfigProcessor>()
