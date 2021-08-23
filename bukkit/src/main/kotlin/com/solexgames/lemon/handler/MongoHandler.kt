package com.solexgames.lemon.handler

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.solexgames.lemon.Lemon
import org.bson.Document

class MongoHandler {

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
            client = MongoClient(MongoClientURI(Lemon.instance.databaseConfig.getString("mongodb.url")))
            database = client.getDatabase(Lemon.instance.databaseConfig.getString("mongodb.database", "SGSoftware"))

            playerCollection = database.getCollection("coreprofiles")
            prefixCollection = database.getCollection("prefix")
            rankCollection = database.getCollection("ranks")
            punishmentCollection = database.getCollection("punishment")
            disguiseCollection = database.getCollection("disguises")

            isConnected = true
            Lemon.instance.logConsole("&a[Mongo] &eConnected to MongoDB!")
        } catch (e: Exception) {
            e.printStackTrace()

            isConnected = false
            Lemon.instance.logConsole("&c[Mongo] &eCouldn't connect to MongoDB.")
        }
    }

    fun close() {
        if (isConnected) {
            client.close()
        }
    }
}