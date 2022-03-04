package gg.scala.lemon.adapter.client.impl

import com.lunarclient.bukkitapi.LunarClientAPI
import com.lunarclient.bukkitapi.nethandler.client.obj.ServerRule
import com.lunarclient.bukkitapi.serverrule.LunarClientAPIServerRule
import gg.scala.lemon.adapter.annotation.RequiredPlugin
import gg.scala.lemon.adapter.client.PlayerClientAdapter
import me.lucko.helper.Events
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author GrowlyX
 * @since 9/27/2021
 */
@RequiredPlugin("LunarClient-API")
class LunarClientAPIAdapter : PlayerClientAdapter
{
    override fun getClientName() = "Lunar Client"

    /**
     * Automatically enable legacy combat & competitive mode.
     */
    init
    {
        LunarClientAPIServerRule.setRule(
            ServerRule.LEGACY_COMBAT, true
        )

        LunarClientAPIServerRule.setRule(
            ServerRule.COMPETITIVE_GAME, true
        )

        Events.subscribe(PlayerJoinEvent::class.java)
            .handler { event ->
                LunarClientAPIServerRule
                    .sendServerRule(event.player)
            }
    }

    override fun shouldHandle(player: Player): Boolean
    {
        return LunarClientAPI.getInstance().isRunningLunarClient(player)
    }

    override fun enableStaffModules(player: Player)
    {
        LunarClientAPI.getInstance().giveAllStaffModules(player)
    }

    override fun disableStaffModules(player: Player)
    {
        LunarClientAPI.getInstance().disableAllStaffModules(player)
    }

    override fun updateNametag(player: Player, tagLines: List<String>)
    {
        Bukkit.getOnlinePlayers().forEach {
            LunarClientAPI.getInstance()
                .overrideNametag(player, tagLines, it)
        }
    }

    override fun resetNametag(player: Player)
    {
        Bukkit.getOnlinePlayers().forEach {
            LunarClientAPI.getInstance()
                .resetNametag(player, it)
        }
    }

    override fun sendTitle(player: Player, title: String, subtitle: String)
    {
        // TODO: 3/3/2022 fix lunar client titles
//        LunarClientAPI.getInstance().sendTitle(player, TitleType.TITLE, title, Duration.ofSeconds(2L))
//        LunarClientAPI.getInstance().sendTitle(player, TitleType.SUBTITLE, subtitle, Duration.ofSeconds(2L))
    }
}
