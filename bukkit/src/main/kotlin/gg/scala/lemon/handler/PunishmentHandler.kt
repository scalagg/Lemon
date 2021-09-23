package gg.scala.lemon.handler

import com.mongodb.client.model.Filters
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.attemptRemoval
import gg.scala.lemon.util.QuickAccess.nameOrConsole
import gg.scala.lemon.util.dispatchToLemon
import gg.scala.lemon.util.other.FancyMessage
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bson.conversions.Bson
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
object PunishmentHandler {

    private fun fetchPunishments(filter: Bson, test: (Punishment) -> Boolean): CompletableFuture<List<Punishment>> {
        return DataStoreHandler.punishmentLayer.fetchAllEntriesWithFilter(filter).thenApply {
            val mutableList = mutableListOf<Punishment>()

            it.forEach { entry ->
                if (test.invoke(entry.value)) {
                    mutableList.add(entry.value)
                }
            }

            return@thenApply mutableList
        }
    }

    fun fetchPunishmentsForTargetOfIntensity(uuid: UUID, intensity: PunishmentCategoryIntensity): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) {
            it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsByExecutorOfIntensity(uuid: UUID, intensity: PunishmentCategoryIntensity): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) {
            it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsForTargetOfCategory(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) {
            it.category == category
        }
    }

    fun fetchPunishmentsForTargetOfCategoryAndActive(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) {
            it.category == category && it.isActive
        }
    }

    fun fetchPunishmentsByExecutorOfCategory(uuid: UUID, category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) {
            it.category == category
        }
    }

    fun fetchAllPunishmentsForTarget(uuid: UUID): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) { true }
    }

    fun fetchAllPunishmentsByExecutor(uuid: UUID): CompletableFuture<List<Punishment>> {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) { true }
    }

    fun fetchExactPunishmentById(uuid: UUID): CompletableFuture<Punishment> {
        return DataStoreHandler.punishmentLayer.fetchEntryByKey(uuid.toString())
    }

    /**
     * Handles warnings exclusively
     *
     * @author GrowlyX
     */
    fun handleWarning(issuer: CommandSender, uuid: UUID, reason: String) {
        Tasks.async {
            val issuerName = nameOrConsole(issuer)
            val targetName = QuickAccess.fetchColoredName(uuid)

            issuer.sendMessage("${CC.GREEN}You've warned $targetName${CC.GREEN} for ${CC.WHITE}$reason${CC.GREEN}.")

            QuickAccess.sendGlobalPlayerMessage(
                """
                    ${CC.RED}You've been warned for ${CC.YELLOW}$reason${CC.RED}!
                """.trimIndent(),
                uuid
            )

            QuickAccess.sendStaffMessage(
                issuer,
                "$issuerName${CC.D_AQUA} warned $targetName${CC.D_AQUA} for ${CC.AQUA}$reason${CC.D_AQUA}.",
                true, QuickAccess.MessageType.NOTIFICATION
            )
        }
    }

    fun handleUnPunishmentForTargetPlayerGlobally(
        issuer: CommandSender, uuid: UUID, reason: String,
        category: PunishmentCategory, silent: Boolean = false
    ) {
        Tasks.async {
            val activePunishments = PunishmentHandler
                .fetchPunishmentsForTargetOfCategoryAndActive(uuid, category)

            val targetName = QuickAccess.fetchColoredName(uuid)
            val issuerUuid = QuickAccess.uuidOf(issuer)

            activePunishments.thenAccept {
                if (it.isNullOrEmpty()) {
                    issuer.sendMessage(arrayOf(
                        "${CC.RED}While attempting to issue an un-punishment for $targetName${CC.RED}",
                        "${CC.RED}there was no pre-existing punishment found."
                    ))
                    return@thenAccept
                }

                attemptRemoval(
                    punishment = it[0],
                    reason = reason,
                    remover = issuerUuid
                )

                handlePostUnPunishmentCheck(
                    it[0], silent, uuid,
                    issuer, issuerUuid, targetName
                )
            }
        }
    }

    /**
     * Handles any type of punishment
     * other than warnings
     *
     * @author GrowlyX
     */
    fun handlePunishmentForTargetPlayerGlobally(
        issuer: CommandSender, uuid: UUID,
        category: PunishmentCategory, duration: Long, reason: String,
        silent: Boolean = false, rePunishing: Boolean = false
    ) {
        Tasks.async {
            val activePunishments = fetchPunishmentsForTargetOfCategoryAndActive(uuid, category)

            val targetName = QuickAccess.fetchColoredName(uuid)
            val targetWeight = QuickAccess.fetchRankWeight(uuid).getNow(0)
            val targetIpInfo = QuickAccess.fetchIpAddress(uuid).getNow(null)

            val issuerUuid = QuickAccess.uuidOf(issuer)
            val issuerWeight = QuickAccess.weightOf(issuer)

            activePunishments.thenAccept {
                if (duration == Long.MAX_VALUE && !category.instant && !issuer.hasPermission("lemon.command.${category.name.toLowerCase()}.permanent")) {
                    issuer.sendMessage("${CC.RED}You do not have permission to issue permanent ${category.name.toLowerCase()}s!")
                    return@thenAccept
                }

                if (!it.isNullOrEmpty() && !rePunishing) {
                    issuer.sendMessage(arrayOf(
                        "${CC.RED}While attempting to issue a punishment for $targetName${CC.RED}",
                        "${CC.RED}there was an active, pre-existing punishment found."
                    ))
                    return@thenAccept
                }

                if (targetWeight >= issuerWeight) {
                    issuer.sendMessage("${CC.RED}Failed to issue a punishment for $targetName${CC.RED} due to them having a higher rank priority than you.")
                    return@thenAccept
                }

                if (rePunishing) {
                    if (!it.isNullOrEmpty()) {
                        issuer.sendMessage(arrayOf(
                            "${CC.RED}While attempting to issue a re-punishment for $targetName${CC.RED}",
                            "${CC.RED}there was no pre-existing punishment found.",
                            "${CC.RED}Please use ${CC.YELLOW}/${category.name.toLowerCase()} ${CC.RED}instead."
                        ))
                        return@thenAccept
                    }

                    attemptRemoval(
                        punishment = it[0],
                        remover = issuerUuid,
                        reason = "Re-${it[0].category.ing}"
                    )
                }

                val punishment = Punishment(
                    UUID.randomUUID(), uuid, targetIpInfo, issuerUuid,
                    System.currentTimeMillis(), Lemon.instance.settings.id,
                    reason, duration, category
                )

                handlePostPunishmentCheck(
                    punishment, silent, uuid,
                    issuer, issuerUuid, targetName
                )
            }
        }
    }

    private fun handlePostPunishmentCheck(punishment: Punishment, silent: Boolean, uuid: UUID, issuer: CommandSender, issuerUuid: UUID?, targetName: String) {
        val issuerName = QuickAccess.fetchColoredName(issuerUuid)

        val broadcastPrefix = if (silent) "${CC.GRAY}(Silent) " else ""
        val broadcastPermission = if (silent) "lemon.staff" else ""
        val broadcastPermanent = if (punishment.isPermanent) "permanently " else ""
        val broadcastSuffix = if (silent) " for ${CC.WHITE}${punishment.addedReason}${CC.GREEN}." else "."

        val broadcastBody = "$broadcastPrefix${CC.YELLOW}$issuerName${CC.GREEN} has $broadcastPermanent${punishment.category.inf} ${CC.YELLOW}$targetName${CC.GREEN}$broadcastSuffix"

        punishment.save().thenRun {
            issuer.sendMessage("$broadcastPrefix${CC.GREEN}You've $broadcastPermanent${punishment.category.inf} ${CC.YELLOW}$targetName${CC.GREEN} for ${CC.WHITE}${punishment.addedReason}${CC.GREEN}.")

            val fancyMessage = FancyMessage()
                .withMessage(broadcastBody)

            if (broadcastPermission.isNotBlank()) {
                fancyMessage.andHoverOf(
                    "${CC.SEC}${CC.STRIKE_THROUGH}-----------------------",
                    "${CC.SEC}Issued By: ${CC.PRI}$issuerName",
                    "${CC.SEC}Issued Reason: ${CC.WHITE}${punishment.addedReason} ${CC.GRAY}(${punishment.expirationString})",
                    "${CC.SEC}Issued On: ${CC.WHITE}${punishment.addedOn}",
                    "${CC.SEC}${CC.STRIKE_THROUGH}-----------------------",
                )
            }

            QuickAccess.sendGlobalFancyBroadcast(
                fancyMessage = fancyMessage,
                permission = broadcastPermission
            ).thenRun {
                RedisHandler.buildMessage(
                    "recalculate-punishments",
                    mutableMapOf<String, String>().also {
                        it["uniqueId"] = uuid.toString()
                    }
                ).dispatchToLemon()

                if (punishment.category == PunishmentCategory.KICK) {
                    RedisHandler.buildMessage(
                        "cross-kick",
                        mutableMapOf<String, String>().also { map ->
                            map["uniqueId"] = punishment.target.toString()
                            map["reason"] = punishment.addedReason
                        }
                    ).dispatchToLemon()
                }
            }
        }
    }

    private fun handlePostUnPunishmentCheck(punishment: Punishment, silent: Boolean, uuid: UUID, issuer: CommandSender, issuerUuid: UUID?, targetName: String) {
        val issuerName = QuickAccess.fetchColoredName(issuerUuid)

        val broadcastPrefix = if (silent) "${CC.GRAY}(Silent) " else ""
        val broadcastPermission = if (silent) "lemon.staff" else ""
        val broadcastSuffix = if (silent) " for ${CC.WHITE}${punishment.removedReason}${CC.GREEN}." else "."

        val broadcastBody = "$broadcastPrefix${CC.YELLOW}$issuerName${CC.GREEN} has un${punishment.category.inf} ${CC.YELLOW}$targetName${CC.GREEN}$broadcastSuffix"

        punishment.save().thenRun {
            issuer.sendMessage("$broadcastPrefix${CC.GREEN}You've un${punishment.category.inf} ${CC.YELLOW}$targetName${CC.GREEN} for ${CC.WHITE}${punishment.removedReason}${CC.GREEN}.")

            val fancyMessage = FancyMessage()
                .withMessage(broadcastBody)

            if (broadcastPermission.isNotBlank()) {
                fancyMessage.andHoverOf(
                    "${CC.SEC}${CC.STRIKE_THROUGH}--------------------",
                    "${CC.SEC}Removed By: ${CC.PRI}$issuerName",
                    "${CC.SEC}Removed Reason: ${CC.WHITE}${punishment.removedReason}",
                    "${CC.SEC}Removed On: ${CC.WHITE}${punishment.removedOn}",
                    "${CC.SEC}${CC.STRIKE_THROUGH}--------------------",
                )
            }

            QuickAccess.sendGlobalFancyBroadcast(
                fancyMessage = fancyMessage,
                permission = broadcastPermission
            ).thenRun {
                RedisHandler.buildMessage(
                    "recalculate-punishments",
                    mutableMapOf<String, String>().also {
                        it["uniqueId"] = uuid.toString()
                    }
                ).dispatchToLemon()
            }
        }
    }
}
