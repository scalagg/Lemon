package com.solexgames.lemon.player

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.enums.PermissionCheck
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.metadata.Metadata
import com.solexgames.lemon.player.note.Note
import com.solexgames.lemon.util.GrantRecalculationUtil
import com.solexgames.lemon.util.other.Cooldown
import com.solexgames.lemon.util.type.Persistent
import net.evilblock.cubed.util.CC
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture


class LemonPlayer(
    var uniqueId: UUID,
    var name: String,
    var ipAddress: String?
): Persistent<Document> {

    var notes = ArrayList<Note>()
    var ignoring = ArrayList<UUID>()

    var commandCooldown = Cooldown(0L)
    var helpOpCooldown = Cooldown(0L)
    var reportCooldown = Cooldown(0L)
    var chatCooldown = Cooldown(0L)
    var slowChatCooldown = Cooldown(0L)

    var lastRecipient: UUID? = null

    lateinit var activeGrant: Grant
    lateinit var country: String

    private var metadata = HashMap<String, Metadata>()

    private fun recalculateGrants() {
        val completableFuture = Lemon.instance.grantHandler.fetchGrantsFor(uniqueId)
        var shouldRecalculate = false

        completableFuture.whenComplete { it, throwable ->
            throwable?.printStackTrace()

            if (it == null || it.isEmpty()) {
                setupAutomaticGrant()

                return@whenComplete
            }

            it.forEach { grant ->
                if (!grant.removed && !grant.hasExpired()) {
                    grant.removedReason = "Expired"
                    grant.removedAt = System.currentTimeMillis()
                    grant.removed = true

                    shouldRecalculate = true
                }
            }

            if (shouldRecalculate) {
                activeGrant = GrantRecalculationUtil.getProminentGrant(it)

                getPlayer().ifPresent { player ->
                    player.sendMessage("${CC.GREEN}Your rank has been set to ${activeGrant.getRank().getColoredName()}${CC.GREEN}.")
                }
            }
        }
    }

    private fun setupAutomaticGrant() {
        val rank = Lemon.instance.rankHandler.getDefaultRank()
        activeGrant = Grant(UUID.randomUUID(), uniqueId, rank.uuid, null, System.currentTimeMillis(), Lemon.instance.settings.id, "Automatic (Lemon)", Long.MAX_VALUE)

        Lemon.instance.grantHandler.registerGrant(activeGrant)
    }

    fun fetchCountry() {
        // TODO: 8/28/2021 use geo location api to fetch & update the players' country to jedis
    }

    fun getColoredName(): String {
        return activeGrant.getRank().color + name
    }

    fun getSetting(id: String): Boolean {
        val data = getMetadata(id)
        return data != null && data.asBoolean()
    }

    fun hasPermission(
        permission: String,
        checkType: PermissionCheck = PermissionCheck.PLAYER
    ): Boolean {
        var hasPermission = false

        when (checkType) {
            PermissionCheck.COMPOUNDED -> hasPermission = activeGrant.getRank().getCompoundedPermissions().contains(permission)
            PermissionCheck.PLAYER -> getPlayer().ifPresent {
                if (it.isOp || it.hasPermission(permission.toLowerCase())) {
                    hasPermission = true
                }
            }
            PermissionCheck.BOTH -> {
                hasPermission = activeGrant.getRank().getCompoundedPermissions().contains(permission)

                getPlayer().ifPresent {
                    if (it.isOp || it.hasPermission(permission.toLowerCase())) {
                        hasPermission = true
                    }
                }
            }
        }

        return hasPermission
    }

    fun resetChatCooldown() {
        val donor = hasPermission("lemon.donator")

        chatCooldown = if (donor) {
            Cooldown(1000L)
        } else {
            Cooldown(3000L)
        }
    }

    fun updateOrAddMetadata(id: String, data: Metadata) {
        metadata[id] = data
    }

    fun removeMetadata(id: String): Metadata? {
        return metadata.remove(id)
    }

    fun hasMetadata(id: String): Boolean {
        return metadata.containsKey(id)
    }

    fun getMetadata(id: String): Metadata? {
        return metadata.getOrDefault(id, null)
    }

    fun isStaff(): Boolean {
        return hasPermission("lemon.staff")
    }

    fun getPlayer(): Optional<Player> {
        return Optional.of(Bukkit.getPlayer(uniqueId))
    }

    override fun save(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val document = Document("_id", uniqueId)
            document["uuid"] = uniqueId.toString()

            document["notes"] = LemonConstants.GSON.toJson(notes)
            document["ignoring"] = LemonConstants.GSON.toJson(ignoring)

            finalizeMetaData()

            document["metadata"] = LemonConstants.GSON.toJson(metadata)
            document["lastRecipient"] = lastRecipient.toString()

            Lemon.instance.mongoHandler.playerCollection.replaceOne(
                Filters.eq("uuid", uniqueId.toString()),
                document, ReplaceOptions().upsert(true)
            )
        }
    }

    private fun finalizeMetaData() {
        updateOrAddMetadata(
            "last-connection", Metadata(System.currentTimeMillis())
        )

        updateOrAddMetadata(
            "current-rank", Metadata(activeGrant.getRank().uuid.toString())
        )
    }

    override fun load(future: CompletableFuture<Document>) {
        future.whenComplete { document, throwable ->
            if (document == null || throwable != null) {
                updateOrAddMetadata(
                    "first-connection", Metadata(System.currentTimeMillis())
                )

                save().whenComplete { _, u ->
                    u?.printStackTrace()
                }

                throwable?.printStackTrace()
                return@whenComplete
            }

            notes = LemonConstants.GSON.fromJson(
                document.getString("notes"), LemonConstants.NOTE_ARRAY_LIST_TYPE
            )
            ignoring = LemonConstants.GSON.fromJson(
                document.getString("ignoring"), LemonConstants.UUID_ARRAY_LIST_TYPE
            )
            metadata = LemonConstants.GSON.fromJson(
                document.getString("metadata"), LemonConstants.STRING_METADATA_MAP_TYPE
            )

            lastRecipient = UUID.fromString(document.getString("lastRecipient"))
        }

        recalculateGrants()
    }

}
