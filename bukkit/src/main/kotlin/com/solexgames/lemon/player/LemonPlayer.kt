package com.solexgames.lemon.player

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.model.Persistent
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.metadata.MetaData
import com.solexgames.lemon.player.note.Note
import com.solexgames.lemon.util.GrantRecalculationUtil
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture

class LemonPlayer(
    uuid: UUID,
    name: String,
    address: String?
): Persistent<Document> {

    var notes: MutableList<Note> = mutableListOf()
    var prefixes: MutableList<String> = mutableListOf()
    var ignoring: MutableList<String> = mutableListOf()
    var permissions: MutableList<String> = mutableListOf()
    var bungeePermissions: MutableList<String> = mutableListOf()

    var uniqueId = uuid
    var username = name
    var ipAddress = address

    var activeGrant: Grant? = null

    private var metaData: MutableMap<String, MetaData> = mutableMapOf()

    fun updateOrAddMetaData(id: String, data: MetaData) {
        metaData[id] = data
    }

    fun getMetaData(id: String): MetaData? {
        return this.metaData.getOrDefault(id, null)
    }

    fun recalculateGrants() {
        this.activeGrant = GrantRecalculationUtil.getProminentGrant(
            Lemon.instance.grantHandler.findGrants(this.uniqueId)
        )
    }

    override fun save(): CompletableFuture<Void> {
        TODO("Not yet implemented")
    }

    override fun load(future: CompletableFuture<Document>) {
        TODO("Not yet implemented")
    }

}
