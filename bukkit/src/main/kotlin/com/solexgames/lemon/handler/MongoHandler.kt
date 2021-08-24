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

    init {
        try {
            client = MongoClient(MongoClientURI(Lemon.instance.mongoConfig.uri))
            database = client.getDatabase(Lemon.instance.mongoConfig.database)

            playerCollection = database.getCollection("coreprofiles")
            prefixCollection = database.getCollection("prefix")
            rankCollection = database.getCollection("ranks")
            punishmentCollection = database.getCollection("punishment")
            disguiseCollection = database.getCollection("disguises")

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
