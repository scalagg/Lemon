package gg.scala.lemon.library

import gg.scala.lemon.library.model.Library
import me.lucko.helper.maven.LibraryLoader
import org.bukkit.craftbukkit.libs.jline.internal.Log
import java.lang.reflect.Modifier

/**
 * @author GrowlyX
 * @since 10/14/2021
 */
object HelperLibraryHandler
{

    /**
     * Common maven repositories, consists
     * of spigot-related repositories.
     */
    @JvmStatic
    val COMMON_REPOSITORIES = listOf(
        "https://jitpack.io",
        "https://repo1.maven.org/maven2",
        "https://repo.dmulloy2.net/nexus/repository/public",
        "https://maven.enginehub.org/repo/",
        "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
    )

    fun loadAll(any: Any)
    {
        loadAll(any::class.java, any)
    }

    /**
     * Scans and registers all declared fields in a class
     * which are [Library] instances.
     */
    fun loadAll(clazz: Class<*>, any: Any? = null)
    {
        val fields = clazz.fields

        for (field in fields)
        {
            if (!field.isAccessible)
            {
                field.isAccessible = true
            }

            val instance = field.get(any)

            if (instance is Library)
            {
                if (instance.forcedRepository != null)
                {
                    LibraryLoader.load(
                        instance.groupId,
                        instance.artifactId,
                        instance.version,
                        instance.forcedRepository!!
                    )
                } else {
                    var foundRepository = false

                    for (commonRepository in COMMON_REPOSITORIES)
                    {
                        try {
                            LibraryLoader.load(
                                instance.groupId,
                                instance.artifactId,
                                instance.version,
                                commonRepository
                            )

                            foundRepository = true
                        } catch (exception: Exception)
                        {
                            println(exception.message)
                        }
                    }

                    if (!foundRepository)
                    {
                        Log.error("Failed to register ${instance.artifactId} version ${instance.version} due to no matching repository being found.")
                    }
                }
            } else
            {
                Log.info("Failed to load ${field.name} due to it not being an instance of Library.")
            }
        }
    }
}
