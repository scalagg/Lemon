package gg.scala.lemon.library

import me.lucko.helper.maven.LibraryLoader
import me.lucko.helper.maven.MavenLibrary

/**
 * @author GrowlyX
 * @since 10/14/2021
 */
@MavenLibrary(
    groupId = "com.github.mkotb",
    artifactId = "ConfigAPI",
    version = "e1c8df3f13"
)
object LemonLibraryLoader
{

    fun initialLoad()
    {
        LibraryLoader.loadAll(this)
    }
}
