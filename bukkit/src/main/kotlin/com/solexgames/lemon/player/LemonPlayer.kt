package com.solexgames.lemon.player

import com.solexgames.lemon.model.Persistent
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.metadata.MetaData
import com.solexgames.lemon.player.note.Note
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture

class LemonPlayer(
    uuid: UUID,
    name: String,
    address: String?
): Persistent<Document> {

    var grants: List<Grant> = mutableListOf()
    var notes: List<Note> = mutableListOf()
    var prefixes: List<String> = mutableListOf()
    var ignoring: List<String> = mutableListOf()
    var permissions: List<String> = mutableListOf()
    var bungeePermissions: List<String> = mutableListOf()

    var uniqueId = uuid
    var username = name
    var ipAddress = address

    private var metaData: MutableMap<String, MetaData> = mutableMapOf()

    fun updateOrAddMetaData(id: String, data: MetaData) {
        metaData[id] = data
    }

    fun getMetaData(id: String): MetaData? {
        return this.metaData.getOrDefault(id, null)
    }

    override fun save(): CompletableFuture<Void> {
        TODO("Not yet implemented")
    }

    override fun load(future: CompletableFuture<Document>) {
        TODO("Not yet implemented")
    }

}
