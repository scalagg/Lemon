package gg.scala.lemon.configuration.editor

import gg.scala.common.Savable
import gg.scala.lemon.Lemon
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/21/2021
 */
abstract class ConfigValueEditor(
    val config: Any,
    val fileName: String,
    val id: String,
): Savable
{
    abstract fun onReload()

    override fun save(): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            Lemon.instance.configFactory.save(fileName, config); onReload()
        }
    }

}
