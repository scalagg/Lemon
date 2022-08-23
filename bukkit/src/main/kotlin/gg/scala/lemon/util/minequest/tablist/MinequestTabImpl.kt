package gg.scala.lemon.util.minequest.tablist

import com.mojang.authlib.GameProfile
import gg.scala.commons.tablist.TablistPopulator
import gg.scala.lemon.Lemon
import gg.scala.lemon.util.MinequestLogic
import gg.scala.lemon.util.QuickAccess
import io.github.nosequel.tab.shared.entry.TabElement
import io.github.nosequel.tab.shared.entry.TabEntry
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/22/2022
 */
object MinequestTabImpl : TablistPopulator
{
    override fun displayPreProcessor(player: Player) =
        CompletableFuture.completedFuture(
            Lemon.instance.lemonWebData.serverName == "Minequest"
        )!!

    private val mappings = mutableMapOf(
        0..19 to 0,
        20..39 to 1,
        40..69 to 2,
        70..79 to 3,
    )

    override fun populate(player: Player, element: TabElement)
    {
        if (Lemon.instance.lemonWebData.serverName != "Minequest")
            return

        element.header = " \n${CC.B_AQUA}MINEQUEST\n "
        element.footer = " \n${CC.D_GRAY}The first multi-version Minecraft social hub!\n${
            "${
                MinequestLogic.getTranslatedName("discord", "blue")
            }${CC.AQUA}.${
                MinequestLogic.getTranslatedName("minequest", "blue")
            }${CC.AQUA}.${
                MinequestLogic.getTranslatedName("gg", "blue")
            }" // ${CC.AQUA}www.minequest.gg/discord
        }\n "

        Bukkit.getOnlinePlayers()
            .sortedByDescending {
                QuickAccess.realRank(it).weight
            }
            .take(80)
            .forEachIndexed { index, other ->
                val gameProfile = MinecraftReflection
                    .getGameProfile(other) as GameProfile

                val textures = gameProfile.properties
                    .get("textures").firstOrNull()
                    ?: return@forEachIndexed

                val mappings = this.mappings.entries
                    .firstOrNull {
                        it.key.contains(index)
                    }?.value ?: 0

                val entry = TabEntry(
                    mappings, if (mappings == 0) index else index / mappings,
                    "${other.playerListName}${other.displayName}",
                    MinecraftReflection.getPing(other),
                    arrayOf(
                        textures.value, textures.signature
                    )
                )

                element.add(entry)
            }
    }
}
