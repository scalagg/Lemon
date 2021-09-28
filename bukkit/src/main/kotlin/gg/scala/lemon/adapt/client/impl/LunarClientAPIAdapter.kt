package gg.scala.lemon.adapt.client.impl

import com.lunarclient.bukkitapi.LunarClientAPI
import com.lunarclient.bukkitapi.`object`.TitleType
import gg.scala.lemon.adapt.client.PlayerClientAdapter
import org.bukkit.entity.Player
import java.time.Duration

/**
 * @author GrowlyX
 * @since 9/27/2021
 */
class LunarClientAPIAdapter : PlayerClientAdapter {

    override fun getClientName() = "Lunar Client"

    override fun shouldHandle(player: Player): Boolean {
        return LunarClientAPI.getInstance().isRunningLunarClient(player)
    }

    override fun enableStaffModules(player: Player) {
        LunarClientAPI.getInstance().giveAllStaffModules(player)
    }

    override fun disableStaffModules(player: Player) {
        LunarClientAPI.getInstance().disableAllStaffModules(player)
    }

    override fun sendTitle(player: Player, title: String, subtitle: String) {
        LunarClientAPI.getInstance().sendTitle(player, TitleType.TITLE, title, Duration.ofSeconds(2L))
        LunarClientAPI.getInstance().sendTitle(player, TitleType.SUBTITLE, subtitle, Duration.ofSeconds(2L))
    }
}
