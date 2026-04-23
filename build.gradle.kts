import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "2.0.0"
    kotlin("kapt") version "2.0.0"
    id("io.github.goooler.shadow") version "8.1.8"
    id("org.ajoberstar.grgit") version "4.1.1"
}

var currentBranch: String = grgit.branch.current().name

allprojects {
    group = "gg.scala.lemon"
    version = "2.0.1${
        if (currentBranch == "develop") "-dev" else ""
    }"

    repositories {
        mavenLocal()
        mavenCentral()
        configureScalaRepository()

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://jitpack.io")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.ajoberstar.grgit")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "io.github.goooler.shadow")
    apply(plugin = "maven-publish")

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly(kotlin("reflect"))

        compileOnly("gg.scala.commons:bukkit:4.0.7")
        compileOnly("gg.scala.store:spigot:1.0.0")
        compileOnly("gg.scala.spigot:server:1.1.0")
        compileOnly("gg.scala.cloudsync:spigot:1.0.5")
    }

    kotlin {
        jvmToolchain(jdkVersion = 21)
    }

    tasks {
        withType<ShadowJar> {
            dependencies {
                exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
                exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
            }

            archiveClassifier.set("all")
            exclude(
                "**/*.kotlin_metadata",
                "**/*.kotlin_builtins",
                "META-INF/"
            )

            archiveFileName = "Lemon.jar"
        }

        withType<KotlinCompile> {
            kotlinOptions.javaParameters = true
            kotlinOptions.jvmTarget = "21"
        }

        withType<JavaCompile> {
            options.compilerArgs.add("-parameters")
            options.fork()
            options.encoding = "UTF-8"
        }
    }

    publishing {
        repositories {
            mavenLocal()
            configureScalaRepository(
                dev = version.toString().endsWith("-dev")
            )
        }

        publications {
            register(
                name = "mavenJava",
                type = MavenPublication::class
            ) {
                from(components["java"])
            }
        }
    }

    tasks.getByName("build")
        .dependsOn(
            "shadowJar"
        )
}

fun RepositoryHandler.configureScalaRepository(dev: Boolean = false)
{
    maven("${property("artifactory_contextUrl")}/gradle-${if (dev) "dev" else "release"}") {
        name = "scala"
        credentials {
            username = property("artifactory_user").toString()
            password = property("artifactory_password").toString()
        }
    }
}
