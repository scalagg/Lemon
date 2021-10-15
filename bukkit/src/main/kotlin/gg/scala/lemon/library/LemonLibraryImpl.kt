package gg.scala.lemon.library

import gg.scala.commons.library.model.Library

/**
 * @author GrowlyX
 * @since 10/14/2021
 */
object LemonLibraryImpl
{

    @JvmStatic
    val CONFIG_API = Library(
        groupId = "com.github.mkotb",
        artifactId = "ConfigAPI",
        version = "ac23157173",
    ).repository(
        "https://jitpack.io"
    )

}
