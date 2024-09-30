dependencies {
    kapt("gg.scala.commons:bukkit:3.4.3")
    api(project(":common"))

    compileOnly("com.github.mkotb:ConfigAPI:e1c8df3f13")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("com.github.LunarClient:BukkitAPI:1.0.1")

// https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("club.minnced:discord-webhooks:0.8.4")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
}

kapt {
    keepJavacAnnotationProcessors = true
}
