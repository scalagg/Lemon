package gg.scala.lemon.adapt.client.impl

import com.oldcheatbreaker.api.CheatBreakerAPI
import com.oldcheatbreaker.api.`object`.TitleType
import gg.scala.lemon.adapt.client.PlayerClientAdapter
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration

/**
 * @author GrowlyX
 * @since 9/27/2021
 */
class CheatBreakerAPIAdapter : PlayerClientAdapter {

    override fun getClientName() = "CheatBreaker+"

    override fun shouldHandle(player: Player): Boolean {
        return CheatBreakerAPI.getInstance().isRunningCheatBreaker(player)
    }

    override fun enableStaffModules(player: Player) {
        CheatBreakerAPI.getInstance().staffModuleHandler.giveAllStaffModules(player)
    }

    override fun disableStaffModules(player: Player) {
        CheatBreakerAPI.getInstance().staffModuleHandler.disableAllStaffModules(player)
    }

    override fun updateNametag(player: Player, tagLines: List<String>) {
        Bukkit.getOnlinePlayers().forEach {
            CheatBreakerAPI.getInstance().nametagHandler
                .overrideNametag(player, tagLines, it)
        }
    }

    override fun resetNametag(player: Player) {
        Bukkit.getOnlinePlayers().forEach {
            CheatBreakerAPI.getInstance().nametagHandler
                .resetNametag(player, it)
        }
    }

    override fun sendTitle(player: Player, title: String, subtitle: String) {
        CheatBreakerAPI.getInstance().titleHandler.sendTitle(player, TitleType.TITLE, title, Duration.ofSeconds(2L))
        CheatBreakerAPI.getInstance().titleHandler.sendTitle(player, TitleType.SUBTITLE, subtitle, Duration.ofSeconds(2L))
    }
}
