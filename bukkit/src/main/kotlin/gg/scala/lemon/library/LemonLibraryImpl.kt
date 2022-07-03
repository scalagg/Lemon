package gg.scala.lemon.library

import gg.scala.commons.annotations.LibraryLoader
import gg.scala.commons.library.model.Library

/**
 * @author GrowlyX
 * @since 10/14/2021
 */
@Library(
    groupId = "com.github.mkotb",
    artifactId = "ConfigAPI",
    version = "ac23157173",
    repository = "https://jitpack.io/"
)
@Library(
    groupId = "com.orbitz.consul",
    artifactId = "consul-client",
    "1.5.3"
)
@Library(
    groupId = "com.squareup.retrofit2",
    artifactId = "converter-jackson",
    "2.9.0"
)
@Library(
    groupId = "com.fasterxml.jackson.core",
    artifactId = "jackson-databind",
    "2.13.3"
)
@LibraryLoader
object LemonLibraryImpl
