package com.solexgames.lemon.processor

import xyz.mkotb.configapi.comment.Comment

object MongoDBConfigProcessor {

    @Comment("What should the mongo URI be?")
    val uri: String = "mongodb://localhost:27017"

    @Comment("What database should we store data in?")
    val database: String = "SGSoftware"

}
