package gg.scala.lemon.command.management

import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Default
import net.evilblock.cubed.acf.annotation.HelpCommand

/**
 * @author GrowlyX
 * @since 10/17/2021
 */
@CommandPermission("op")
@CommandAlias("permission")
class PermissionCommand : BaseCommand()
{

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }


}
