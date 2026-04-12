package gg.scala.lemon.adapter.client.impl

import com.lunarclient.bukkitapi.LunarClientAPI
import com.lunarclient.bukkitapi.nethandler.client.LCPacketModSettings
import com.lunarclient.bukkitapi.nethandler.client.obj.ModSettings
import com.lunarclient.bukkitapi.nethandler.client.obj.ServerRule
import com.lunarclient.bukkitapi.serverrule.LunarClientAPIServerRule
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.Lemon
import gg.scala.lemon.adapter.client.PlayerClientAdapter
import gg.scala.lemon.minequest
import me.lucko.helper.Events
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent

@Service
@IgnoreAutoScan
@SoftDependency(value = "LunarClient-API")
class LunarClientAPIAdapter : PlayerClientAdapter
{
    @Inject
    lateinit var plugin: Lemon

    override fun getClientName() = "Lunar Client"

    @Configure
    fun configure()
    {
        LunarClientAPIServerRule.setRule(
            ServerRule.LEGACY_COMBAT, true
        )

        LunarClientAPIServerRule.setRule(
            ServerRule.COMPETITIVE_GAME, true
        )

        val settings = LCPacketModSettings(
            ModSettings().addModSetting(
                "one_seven_visuals",
                ModSettings.ModSetting(false, hashMapOf())
            )
        )

        Events.subscribe(PlayerJoinEvent::class.java)
            .handler { event ->
                if (minequest() && "duels" !in Lemon.instance.settings.id)
                {
                    LunarClientAPI.getInstance()
                        .sendPacket(
                            event.player, settings
                        )
                }

                LunarClientAPIServerRule
                    .sendServerRule(event.player)
            }

        this.plugin.clientAdapters.add(this)
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

    }
}
