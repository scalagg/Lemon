dependencies {
    compileOnly(kotlin("stdlib"))
    kapt("gg.scala.commons:bukkit:4.0.7")
    kapt("gg.scala.store:spigot:1.0.0")
    kapt("gg.scala.store:shared:1.0.0")
    kapt("gg.scala.aware:aware:2.0.0")
    api(project(":common"))

    compileOnly("gg.scala.store:spigot:1.0.0")
    compileOnly("gg.scala.store:shared:1.0.0")
    compileOnly("gg.scala.aware:aware:2.0.0")

    
    compileOnly("gg.scala.commons:commons:4.0.7")
    compileOnly("gg.scala.flavor:flavor:0.1.6")
    compileOnly("me.lucko:helper:5.6.14")

    compileOnly("org.mongodb:mongo-java-driver:3.12.11")

    compileOnly("com.github.mkotb:ConfigAPI:e1c8df3f13")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("gg.scala.game.extensions:scala-core-game-extensions:1.5.2")
    compileOnly("com.github.LunarClient:BukkitAPI:1.0.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("club.minnced:discord-webhooks:0.8.4")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
}

kapt {
    keepJavacAnnotationProcessors = true
    correctErrorTypes = true
}
