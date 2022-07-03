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
    version = "1.5.3"
)
@Library(
    groupId = "com.squareup.retrofit2",
    artifactId = "converter-jackson",
    version = "2.9.0"
)
@Library(
    groupId = "com.fasterxml.jackson.core",
    artifactId = "jackson-databind",
    version = "2.13.3"
)
@Library(
    groupId = "com.fasterxml.jackson.datatype",
    artifactId = "jackson-datatype-jdk8",
    version = "2.11.1"
)
@Library(
    groupId = "com.fasterxml.jackson.datatype",
    artifactId = "jackson-datatype-guava",
    version = "2.13.3"
)
@Library(
    groupId = "com.fasterxml.jackson.core",
    artifactId = "jackson-annotations",
    version = "2.13.3"
)
@Library(
    groupId = "com.fasterxml.jackson.core",
    artifactId = "jackson-core",
    version = "2.13.3"
)
@LibraryLoader
object LemonLibraryImpl
