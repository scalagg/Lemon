package gg.scala.lemon.library.model

import me.lucko.helper.maven.MavenLibrary
import gg.scala.lemon.library.HelperLibraryHandler

/**
 * An object form of the [MavenLibrary]
 * annotation in helper. Should only be
 * used for [HelperLibraryHandler]
 * dependency processing.
 *
 * @author GrowlyX
 * @since 10/14/2021
 */
data class Library(
    val groupId: String,
    val artifactId: String,
    val version: String,
)
{
    var forcedRepository: String? = null

    fun repository(url: String): Library
    {
        forcedRepository = url; return this
    }
}
