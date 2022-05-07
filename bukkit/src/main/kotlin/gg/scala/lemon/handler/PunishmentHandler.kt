package gg.scala.lemon.handler

import com.mongodb.client.model.Filters
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.attemptRemoval
import gg.scala.lemon.util.QuickAccess.fetchColoredName
import gg.scala.lemon.util.QuickAccess.fetchIpAddress
import gg.scala.lemon.util.QuickAccess.nameOrConsole
import gg.scala.lemon.util.QuickAccess.sendGlobalFancyBroadcast
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.MongoDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.md_5.bungee.api.chat.ClickEvent
import org.bson.conversions.Bson
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
object PunishmentHandler
{
    private fun fetchPunishments(
        filter: Bson, test: (Punishment) -> Boolean
    ): CompletableFuture<List<Punishment>>
    {
        val controller = DataStoreObjectControllerCache.findNotNull<Punishment>()

        return controller.useLayerWithReturn<MongoDataStoreStorageLayer<Punishment>, CompletableFuture<List<Punishment>>>(
            DataStoreStorageType.MONGO
        ) {
            return@useLayerWithReturn this.loadAllWithFilter(filter).thenApply {
                val mutableList = mutableListOf<Punishment>()

                it.forEach { entry ->
                    if (test.invoke(entry.value)) {
                        mutableList.add(entry.value)
                    }
                }

                return@thenApply mutableList
            }
        }
    }

    fun fetchPunishmentsForTargetOfIntensity(
        uuid: UUID,
        intensity: PunishmentCategoryIntensity
    ): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) {
            it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsByExecutorOfIntensity(
        uuid: UUID,
        intensity: PunishmentCategoryIntensity
    ): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) {
            it.isIntensity(intensity)
        }
    }

    fun fetchPunishmentsForTargetOfCategory(
        uuid: UUID,
        category: PunishmentCategory
    ): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) {
            it.category == category
        }
    }

    fun fetchPunishmentsForTargetOfCategoryAndActive(
        uuid: UUID,
        category: PunishmentCategory
    ): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) {
            it.category == category && it.isActive
        }
    }

    fun fetchPunishmentsByExecutorOfCategory(
        uuid: UUID,
        category: PunishmentCategory
    ): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) {
            it.category == category
        }
    }

    fun fetchPunishmentsRemovedByOfCategory(
        uuid: UUID,
        category: PunishmentCategory
    ): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("removedBy", uuid.toString())
        ) {
            it.category == category
        }
    }

    fun fetchPunishmentsRemovedBy(uuid: UUID): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("removedBy", uuid.toString())
        ) {
            true
        }
    }

    fun fetchAllPunishmentsForTarget(uuid: UUID): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("target", uuid.toString())
        ) { true }
    }

    fun fetchAllPunishmentsByExecutor(uuid: UUID): CompletableFuture<List<Punishment>>
    {
        return fetchPunishments(
            Filters.eq("addedBy", uuid.toString())
        ) { true }
    }

    fun fetchExactPunishmentById(uuid: UUID): CompletableFuture<Punishment?>
    {
        return DataStoreObjectControllerCache.findNotNull<Punishment>()
            .load(uuid, DataStoreStorageType.MONGO)
    }

    /**
     * Handles warnings exclusively
     *
     * @author GrowlyX
     */
    fun handleWarning(issuer: CommandSender, uuid: UUID, reason: String): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            val issuerName = nameOrConsole(issuer)
            val targetName = fetchColoredName(uuid)

            issuer.sendMessage("${CC.GREEN}You've warned $targetName${CC.GREEN} for ${CC.WHITE}$reason${CC.GREEN}.")

            QuickAccess.sendGlobalPlayerMessage(
                """
                    ${CC.RED}You've been warned for ${CC.YELLOW}$reason${CC.RED}!
                """.trimIndent(),
                uuid
            )

            QuickAccess.sendStaffMessage(
                "$issuerName${CC.D_AQUA} warned $targetName${CC.D_AQUA} for ${CC.AQUA}$reason${CC.D_AQUA}.", true
            )
        }
    }

    @JvmOverloads
    fun handleUnPunishmentForTargetPlayerGlobally(
        issuer: CommandSender, uuid: UUID, reason: String,
        category: PunishmentCategory, silent: Boolean = false
    )
    {
        val activePunishments = fetchPunishmentsForTargetOfCategoryAndActive(uuid, category)

        val targetName = fetchColoredName(uuid)
        val issuerUuid = QuickAccess.uuidOf(issuer)

        activePunishments.thenAccept {
            if (it.isNullOrEmpty())
            {
                issuer.sendMessage(
                    "${CC.RED}$targetName${CC.RED} does not have an active punishment within this category."
                )
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

    /**
     * Handles any type of punishment
     * other than warnings
     *
     * @author GrowlyX
     */
    @JvmOverloads
    fun handlePunishmentForTargetPlayerGlobally(
        issuer: CommandSender, uuid: UUID,
        category: PunishmentCategory, duration: Long, reason: String,
        silent: Boolean = false, rePunishing: Boolean = false
    )
    {
        if (issuer is Player)
        {
            if (issuer.uniqueId == uuid)
            {
                issuer.sendMessage("${CC.RED}You're not allowed to issue punishments towards yourself.")
                return
            }
        }

        if (category != PunishmentCategory.BLACKLIST && category != PunishmentCategory.KICK)
        {
            if (duration == Long.MAX_VALUE && !category.instant && !issuer.hasPermission("lemon.command.${category.name.lowercase()}.permanent"))
            {
                issuer.sendMessage("${CC.RED}You do not have permission to issue permanent ${category.name.lowercase()}s!")
                return
            }
        }

        ForkJoinPool.commonPool().execute {
            val activePunishments = fetchPunishmentsForTargetOfCategoryAndActive(uuid, category)

            val targetName = fetchColoredName(uuid)

            val issuerUuid = QuickAccess.uuidOf(issuer)
            val issuerWeight = QuickAccess.weightOf(issuer)

            QuickAccess.fetchRankWeight(uuid).thenApply { targetWeight ->
                activePunishments.thenAccept {
                    if (!it.isNullOrEmpty() && !rePunishing)
                    {
                        issuer.sendMessage(
                            "${CC.B_RED}$targetName${CC.RED} already has an active punishment within this category."
                        )
                        return@thenAccept
                    }

                    if (targetWeight >= issuerWeight)
                    {
                        issuer.sendMessage("${CC.RED}You do not have enough power to ${category.name.lowercase()} $targetName${CC.RED}.")
                        return@thenAccept
                    }

                    if (rePunishing)
                    {
                        if (it.isNullOrEmpty())
                        {
                            issuer.sendMessage(
                                arrayOf(
                                    "${CC.RED}$targetName${CC.RED} does not have an active punishment within this category.",
                                    "${CC.RED}Please use ${CC.BOLD}/${category.name.lowercase()}${CC.RED} instead."
                                )
                            )
                            return@thenAccept
                        }

                        attemptRemoval(
                            punishment = it[0],
                            remover = issuerUuid,
                            reason = "Re-${it[0].category.ing}"
                        )

                        handlePostUnPunishmentCheck(it[0], silent, uuid, issuer, issuerUuid, targetName) {
                            continuedPrePunishmentHandling(
                                issuer, uuid, category, duration, reason, silent, issuerUuid, targetName
                            )
                        }
                    } else
                    {
                        continuedPrePunishmentHandling(
                            issuer, uuid, category, duration, reason, silent, issuerUuid, targetName
                        )
                    }
                }
            }
        }
    }

    private fun continuedPrePunishmentHandling(
        issuer: CommandSender, uuid: UUID,
        category: PunishmentCategory, duration: Long, reason: String,
        silent: Boolean = false, issuerUuid: UUID?, targetName: String
    )
    {
        fetchIpAddress(uuid).thenAccept { ipAddress ->
            val punishment = Punishment(
                UUID.randomUUID(), uuid, ipAddress, issuerUuid,
                System.currentTimeMillis(), Lemon.instance.settings.id,
                reason, duration, category
            )

            handlePostPunishmentCheck(
                punishment, silent, uuid,
                issuer, issuerUuid, targetName
            )
        }
    }

    private fun handlePostPunishmentCheck(
        punishment: Punishment, silent: Boolean, uuid: UUID,
        issuer: CommandSender, issuerUuid: UUID?, targetName: String
    )
    {
        val issuerName = fetchColoredName(issuerUuid)

        val broadcastPrefix = if (silent) "${CC.GRAY}(Silent) " else ""
        val broadcastPermission = if (silent) "lemon.staff" else null
        val broadcastPermanent = if (punishment.isPermanent) "permanently " else "temporarily "
        val broadcastSuffix = if (!punishment.isPermanent) " for ${punishment.durationString}${CC.GREEN}." else "."

        val broadcastBody =
            "$broadcastPrefix${CC.YELLOW}$issuerName${CC.GREEN} has $broadcastPermanent${punishment.category.inf} ${CC.YELLOW}$targetName${CC.GREEN}$broadcastSuffix"

        punishment.save().thenRun {
            issuer.sendMessage("$broadcastPrefix${CC.GREEN}You've $broadcastPermanent${punishment.category.inf} ${CC.YELLOW}$targetName${CC.GREEN} for ${CC.WHITE}${punishment.addedReason}${CC.GREEN}.")

            val fancyMessage = FancyMessage()
                .withMessage(broadcastBody)

            fancyMessage.andHoverOf(
                "${CC.SEC}${CC.STRIKE_THROUGH}-----------------------",
                "${CC.SEC}Issued By: ${CC.PRI}$issuerName ${CC.GRAY}(${punishment.addedOn})",
                "${CC.SEC}Issued Reason: ${CC.WHITE}${punishment.addedReason}",
                "${CC.SEC}${CC.STRIKE_THROUGH}-----------------------",
            )

            fancyMessage.andCommandOf(
                ClickEvent.Action.RUN_COMMAND,
                "/history ${punishment.target}"
            )

            sendGlobalFancyBroadcast(
                fancyMessage = fancyMessage,
                permission = broadcastPermission,
                metaPermission = "lemon.staff"
            ).thenRun {
                RedisHandler.buildMessage(
                    "recalculate-punishments",
                    "uniqueId" to uuid.toString()
                ).publish()

                if (punishment.category == PunishmentCategory.KICK)
                {
                    RedisHandler.buildMessage(
                        "cross-kick",
                        "uniqueId" to uuid.toString(),
                        "reason" to punishment.addedReason
                    ).publish()
                }
            }
        }
    }

    private fun handlePostUnPunishmentCheck(
        punishment: Punishment,
        silent: Boolean,
        uuid: UUID,
        issuer: CommandSender,
        issuerUuid: UUID?,
        targetName: String,
        completed: (Boolean) -> Unit = {}
    )
    {
        val issuerName = fetchColoredName(issuerUuid)

        val broadcastPrefix = if (silent) "${CC.GRAY}(Silent) " else ""
        val broadcastPermission = if (silent) "lemon.staff" else null
        val broadcastSuffix = if (silent) " for ${CC.WHITE}${punishment.removedReason}${CC.GREEN}." else "."

        val broadcastBody =
            "$broadcastPrefix${CC.YELLOW}$issuerName${CC.GREEN} has un${punishment.category.inf} ${CC.YELLOW}$targetName${CC.GREEN}$broadcastSuffix"

        punishment.save().thenRun {
            issuer.sendMessage("$broadcastPrefix${CC.GREEN}You've un${punishment.category.inf} ${CC.YELLOW}$targetName${CC.GREEN} for ${CC.WHITE}${punishment.removedReason}${CC.GREEN}.")

            val fancyMessage = FancyMessage()
                .withMessage(broadcastBody)

            val coloredNameOfAddedBy = fetchColoredName(punishment.addedBy)

            fancyMessage.andHoverOf(
                "${CC.SEC}${CC.STRIKE_THROUGH}--------------------",
                "${CC.SEC}Issued By: ${CC.PRI}$coloredNameOfAddedBy ${CC.GRAY}(${punishment.addedOn})",
                "${CC.SEC}Issued Reason: ${CC.WHITE}${punishment.addedReason}",
                "${CC.SEC}Removed By: ${CC.PRI}$issuerName ${CC.GRAY}(${punishment.removedOn})",
                "${CC.SEC}Removed Reason: ${CC.WHITE}${punishment.removedReason}",
                "${CC.SEC}${CC.STRIKE_THROUGH}--------------------",
            )

            fancyMessage.andCommandOf(
                ClickEvent.Action.RUN_COMMAND,
                "/history ${punishment.target}"
            )

            sendGlobalFancyBroadcast(
                fancyMessage = fancyMessage,
                permission = broadcastPermission,
                metaPermission = "lemon.staff"
            ).thenRun {
                RedisHandler.buildMessage(
                    "recalculate-punishments",
                    "uniqueId" to uuid.toString()
                ).publish(AwareThreadContext.SYNC)

                completed.invoke(true)
            }
        }
    }
}
