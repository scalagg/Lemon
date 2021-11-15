package gg.scala.lemon.adapter.client.impl

import com.lunarclient.bukkitapi.LunarClientAPI
import com.lunarclient.bukkitapi.`object`.TitleType
import gg.scala.lemon.adapter.annotation.RequiredPlugin
import gg.scala.lemon.adapter.client.PlayerClientAdapter
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration

/**
 * @author GrowlyX
 * @since 9/27/2021
 */
@RequiredPlugin("LunarClient-API")
class LunarClientAPIAdapter : PlayerClientAdapter
{

    override fun getClientName() = "Lunar Client"

    override fun shouldHandle(player: Player): Boolean
    {
        return LunarClientAPI.getInstance().isRunningLunarClient(player)
    }

    override fun enableStaffModules(player: Player)
    {
        // LunarClient no longer supports staff modules
    }

    override fun disableStaffModules(player: Player)
    {
        // LunarClient no longer supports staff modules
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
        LunarClientAPI.getInstance().sendTitle(player, TitleType.TITLE, title, Duration.ofSeconds(2L))
        LunarClientAPI.getInstance().sendTitle(player, TitleType.SUBTITLE, subtitle, Duration.ofSeconds(2L))
    }
}
