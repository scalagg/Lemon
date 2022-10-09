package gg.scala.lemon.player.entity.superboat

import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.acf.bukkit.contexts.OnlinePlayer
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

/**
 * @author GrowlyX
 * @since 11/25/2021
 */
@CommandAlias("superboat")
@CommandPermission("lemon.command.superboat")
object EntitySuperBoatCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("spawn")
    @CommandCompletion("@players")
    fun onCreate(
        player: Player,
        target: OnlinePlayer,
        size: Int
    ): CompletableFuture<Void>
    {
        val start = Random.nextInt(12000, 19000)

        val entity = WrapperPlayServerSpawnEntity()
        entity.handle.integers.write(0, target.player.location.x.toInt())
        entity.handle.integers.write(1, target.player.location.y.toInt())
        entity.handle.integers.write(2, target.player.location.z.toInt())
        entity.type = WrapperPlayServerSpawnEntity.ObjectTypes.BOAT

        return CompletableFuture
            .runAsync {
                for (i in start..start + size)
                {
                    entity.entityID = i
                    entity.sendPacket(target.player)
                }
            }
            .exceptionally {
                it.printStackTrace()
                return@exceptionally null
            }
            .thenRun {
                player.sendMessage("${CC.GREEN}Spawned ${CC.YELLOW}$size${CC.GREEN} boats.")
            }
    }
}
