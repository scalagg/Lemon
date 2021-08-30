package com.solexgames.lemon.handler

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.solexgames.lemon.Lemon
import org.bson.Document
import java.util.logging.Logger

object MongoHandler {

    var isConnected = false

    lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    lateinit var playerCollection: MongoCollection<Document>
    lateinit var prefixCollection: MongoCollection<Document>
    lateinit var punishmentCollection: MongoCollection<Document>
    lateinit var rankCollection: MongoCollection<Document>
    lateinit var disguiseCollection: MongoCollection<Document>
    lateinit var grantCollection: MongoCollection<Document>

    init {
        try {
            client = MongoClient(MongoClientURI(Lemon.instance.mongoConfig.uri))
            database = client.getDatabase(Lemon.instance.mongoConfig.database)

            playerCollection = database.getCollection("lemon_players")
            prefixCollection = database.getCollection("chat_tags")
            punishmentCollection = database.getCollection("punishments")
            rankCollection = database.getCollection("ranks")
            disguiseCollection = database.getCollection("disguises")
            grantCollection = database.getCollection("grants")

            isConnected = true
        } catch (e: Exception) {
            e.printStackTrace()

            isConnected = false
        }
    }

    fun close() {
        if (isConnected) {
            client.close()
        }
    }
}
