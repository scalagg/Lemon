package gg.scala.lemon.util.minequest.tablist

import com.mojang.authlib.GameProfile
import gg.scala.commons.tablist.TablistPopulator
import gg.scala.lemon.Lemon
import gg.scala.lemon.channel.channels.DefaultChatChannel
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.minequest
import gg.scala.lemon.util.MinequestLogic
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.minequest.platinum.MinequestPlatinumColors
import io.github.nosequel.tab.shared.entry.TabElement
import io.github.nosequel.tab.shared.entry.TabEntry
import io.github.nosequel.tab.shared.skin.SkinType
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.apache.commons.lang3.RandomStringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
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

                val data = if (textures == null)
                    SkinType.LIGHT_GRAY.skinData else arrayOf(
                    textures.value, textures.signature
                )

                val lemonPlayer = PlayerHandler
                    .find(other.uniqueId)
                    ?: return@forEachIndexed

                val rank = QuickAccess.realRank(other)

                val strippedPrefix = ChatColor
                    .stripColor(rank.prefix)

                val strippedSuffix = ChatColor
                    .stripColor(rank.suffix)

                val prefix =
                    (if (strippedPrefix.isNotEmpty())
                        "${rank.prefix} " else "")

                val suffix = if (strippedSuffix.isNotEmpty())
                    " ${rank.suffix}" else ""

                var composed = "$prefix${rank.color}${
                    lemonPlayer.getColoredName()
                }$suffix"

                if (
                    minequest() && rank.name == "Platinum"
                )
                {
                    val current = lemonPlayer
                        .getMetadata("platinum")
                        ?.asString() ?: "default"

                    val mapping = MinequestPlatinumColors[current]
                        ?: MinequestPlatinumColors.values.first()

                    composed = "${mapping.translated} ${mapping.chatColor}${lemonPlayer.name}$suffix"
                }

                val entry = TabEntry(
                    index / 20, index % 20, composed,
                    MinecraftReflection.getPing(other), data
                )

                element.add(entry)
            }
    }
}
