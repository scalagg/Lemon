package gg.scala.lemon.cooldown

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.cooldown.impl.CommandCooldown
import gg.scala.lemon.cooldown.type.PlayerCooldown
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
@Service
object CooldownHandler
{
    private val cooldowns = mutableMapOf<Class<*>, PlayerCooldown>()

    fun find(clazz: Class<*>): PlayerCooldown?
    {
        return cooldowns[clazz]
    }

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler {
                cooldowns.values.forEach { cooldown ->
                    cooldown.reset(it.player)
                }
            }

        register(CommandCooldown)
    }

    fun register(vararg cooldown: PlayerCooldown)
    {
        for (playerCooldown in cooldown)
        {
            cooldowns[playerCooldown.javaClass] = playerCooldown
        }
    }

    fun <T> isActive(clazz: Class<T>, player: Player): Boolean
    {
        val cooldown = cooldowns[clazz] ?: return false

        return cooldown.isActive(player)
    }

    fun <T> notifyAndContinue(clazz: Class<T>, player: Player): Boolean
    {
        return notifyAndContinue(clazz, player, "")
    }

    fun <T> notifyAndContinue(
        clazz: Class<T>, player: Player, action: String
    ): Boolean
    {
        val cooldown = cooldowns[clazz] ?: return true

        return if (cooldown.isActive(player))
        {
            val formatted = cooldown.getRemainingFormatted(player)

            player.sendMessage(
                "${CC.RED}Please wait $formatted before ${
                    if (action == "") cooldown.id() else action
                } again! ${
                    Lemon.instance.languageConfig.cooldownDenyMessageAddition
                }"
            )

            false
        } else true
    }

    fun <T> notifyAndContinueNoBypass(
        clazz: Class<T>, player: Player, action: String
    ): Boolean
    {
        val cooldown = cooldowns[clazz] ?: return true

        return if (cooldown.isActive(player))
        {
            val formatted = cooldown.getRemainingFormatted(player)

            player.sendMessage(
                "${CC.RED}Please wait $formatted before ${
                    if (action == "") cooldown.id() else action
                } again!"
            )

            false
        } else true
    }
}
