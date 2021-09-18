package gg.scala.lemon.processor

import xyz.mkotb.configapi.comment.Comment

class RedisConfigProcessor {

    @Comment("What should the redis address be?")
    val address: String = "127.0.0.1"

    @Comment("What should the redis port be?")
    val port: Int = 6379

    @Comment("Should we use redis authentication?")
    val authentication: Boolean = false

    @Comment("What should the redis password be?")
    val password: String = "password"

}
