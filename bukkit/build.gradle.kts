dependencies {
    kapt("gg.scala.commons:bukkit:3.4.3")
    api(project(":common"))

    compileOnly("com.github.mkotb:ConfigAPI:e1c8df3f13")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("com.github.LunarClient:BukkitAPI:1.0.1")

    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
}

kapt {
    keepJavacAnnotationProcessors = true
}
