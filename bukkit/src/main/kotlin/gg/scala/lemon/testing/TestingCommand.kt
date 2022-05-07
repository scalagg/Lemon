package gg.scala.lemon.testing

import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.Lemon
import gg.scala.lemon.testing.criteria.TestCriteriaMenu
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.*
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/14/2021
 */
@CommandAlias("testing")
@CommandPermission("op")
object TestingCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("criteria")
    @Description("Testing PaginatedCriteriaMenu")
    fun onCriteria(player: Player)
    {
        Lemon.instance.serverLayer
            .loadAll(DataStoreStorageType.REDIS)
            .thenAccept {
                TestCriteriaMenu(it.values.toList()).openMenu(player)
            }
    }
}
