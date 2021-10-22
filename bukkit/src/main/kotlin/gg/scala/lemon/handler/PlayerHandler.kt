package gg.scala.lemon.handler

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.Lemon
import gg.scala.lemon.menu.staff.StaffListMenu
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import me.lucko.helper.Events
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.scoreboard.ScoreboardHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.player.PlayerSnapshot
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import java.util.concurrent.CompletableFuture

object PlayerHandler {

    val inventory = mutableMapOf<Int, ItemStack>()
    val vanishItems = mutableMapOf<Boolean, ItemStack>()
    val snapshots = mutableMapOf<UUID, PlayerSnapshot>()

    var players: HashMap<UUID, LemonPlayer> = hashMapOf()

    init {
        inventory[0] =
            ItemBuilder(XMaterial.BOOK)
                .name("${CC.B_PRI}Inspect Player")
                .addToLore(
                    "${CC.GRAY}Click a player using",
                    "${CC.GRAY}this book to take a look",
                    "${CC.GRAY}at their inventory.",
                ).build()

        inventory[1] =
            ItemBuilder(XMaterial.COMPASS)
                .name("${CC.B_PRI}Push Forward")
                .addToLore(
                    "${CC.GRAY}Click this item to",
                    "${CC.GRAY}push yourself forward."
                ).build()

        inventory[8] =
            ItemBuilder(XMaterial.PAPER)
                .name("${CC.B_PRI}Staff List")
                .addToLore(
                    "${CC.GRAY}View all online staff.",
                ).build()

        vanishItems[true] = ItemBuilder(XMaterial.LIME_DYE)
            .name("${CC.B_PRI}Disable Vanish")
            .data(10)
            .addToLore(
                "${CC.GRAY}Click this item to un-vanish.",
            ).build()

        vanishItems[false] = ItemBuilder(XMaterial.LIGHT_GRAY_DYE)
            .name("${CC.B_PRI}Enable Vanish")
            .data(8)
            .addToLore(
                "${CC.GRAY}Click this item to vanish.",
            ).build()

        Events.subscribe(PlayerInteractEvent::class.java)
            .filter { it.item != null }
            .filter { it.player.hasMetadata("mod-mode") }
            .handler {
                val isHoldingPushForward =  it.item.isSimilar(inventory[1])
                val isHoldingToggleVanish =  it.item.hasItemMeta() && it.item.itemMeta.displayName.contains("Vanish")
                val isHoldingFreeze =  it.item.isSimilar(inventory[8])

                if (isHoldingToggleVanish) {
                    Bukkit.dispatchCommand(
                        it.player,
                        "vanish"
                    )

                    it.player.inventory.setItem(7, vanishItems[it.player.hasMetadata("vanished")])
                    it.player.updateInventory()
                } else if (isHoldingPushForward) {
                    it.player.velocity = it.player.location
                        .direction.multiply(2.5F)
                } else if (isHoldingFreeze) {
                    StaffListMenu().openMenu(it.player)
                }
            }

        Events.subscribe(PlayerInteractAtEntityEvent::class.java)
            .filter { it.rightClicked is Player }
            .filter { it.player.inventory.itemInHand != null }
            .filter { it.player.hasMetadata("mod-mode") }
            .handler {
                val rightClicked = it.rightClicked as Player
                val isHoldingViewInventory =  it.player.inventory.itemInHand.isSimilar(inventory[0])

                // TODO: 10/10/2021 finish view inv
            }
    }

    fun findPlayer(uuid: UUID): Optional<LemonPlayer> {
        return Optional.ofNullable(players[uuid])
    }

    fun findOnlinePlayer(name: String): LemonPlayer? {
        return players.values.firstOrNull {
            it.name.equals(name, true)
        }
    }

    fun findPlayer(name: String): Optional<LemonPlayer> {
        val player = Bukkit.getPlayer(name)

        if (player != null) {
            return Optional.ofNullable(
                players[player.uniqueId]
            )
        }

        return Optional.ofNullable(null)
    }

    fun findPlayer(player: Player?): Optional<LemonPlayer> {
        return Optional.ofNullable(players[player?.uniqueId])
    }

    fun vanishPlayer(player: Player, power: Int = 0) {
        player.setMetadata("vanished", FixedMetadataValue(Lemon.instance, true))
        player.setMetadata("vanish-power", FixedMetadataValue(Lemon.instance, power))

        VisibilityHandler.updateToAll(player)
        NametagHandler.reloadPlayer(player)
    }

    fun unvanishPlayer(player: Player) {
        player.removeMetadata("vanished", Lemon.instance)
        player.removeMetadata("vanish-power", Lemon.instance)

        VisibilityHandler.updateToAll(player)
        NametagHandler.reloadPlayer(player)
    }

    fun modModePlayer(player: Player, target: Player) {
        snapshots[target.uniqueId] = PlayerSnapshot(target)

        target.inventory.clear()

        inventory.forEach {
            target.inventory.setItem(it.key, it.value)
        }

        target.inventory.setItem(7, vanishItems[target.hasMetadata("vanished")])

        target.setMetadata("mod-mode", FixedMetadataValue(Lemon.instance, true))

        player.sendMessage("${CC.SEC}${
            if (player == target) "You are" else CC.SEC + target.name + " is"
        } now ${CC.GREEN}in mod mode${CC.SEC}.")

        NametagHandler.reloadPlayer(target)
    }

    fun unModModePlayer(player: Player, target: Player) {
        unModModePlayerSilent(target)

        player.sendMessage("${CC.SEC}${
            if (player == target) "You are" else CC.SEC + target.name + " is"
        } no longer ${CC.RED}in mod mode${CC.SEC}.")
    }

    fun unModModePlayerSilent(player: Player) {
        player.removeMetadata("mod-mode", Lemon.instance)

        val snapshot = snapshots.remove(player.uniqueId)
        snapshot?.restore(player, teleport = false)
    }

    fun fetchAlternateAccountsFor(uuid: UUID): CompletableFuture<List<LemonPlayer>> {
        return DataStoreHandler.lemonPlayerLayer.fetchAllEntries().thenApply {
            val accounts = mutableListOf<LemonPlayer>()
            val lemonPlayer = findPlayer(uuid).orElse(null)

            for (entry in it) {
                if (entry.value.uniqueId == uuid) continue

                lemonPlayer.pastIpAddresses.keys.forEachIndexed { _, address ->
                    if (entry.value.pastIpAddresses.containsKey(address)) {
                        accounts.add(entry.value)
                        return@forEachIndexed
                    }
                }
            }

            return@thenApply accounts
        }
    }

    fun getCorrectedPlayerList(sender: CommandSender): Collection<LemonPlayer> {
        var currentList = Bukkit.getOnlinePlayers()
            .mapNotNull {
                findPlayer(it.uniqueId).orElse(null)
            }.sortedBy {
                -QuickAccess.realRank(it.bukkitPlayer!!).weight
            }

        if (currentList.size > 350) {
            currentList = currentList.subList(0, 350) as ArrayList<LemonPlayer>
        }

        if (sender.hasPermission("lemon.staff")) {
            return currentList
        }

        return currentList.filter {
            !it.bukkitPlayer!!.hasMetadata("vanished") && !it.bukkitPlayer!!.hasMetadata("mod-mode")
        }
    }

}
