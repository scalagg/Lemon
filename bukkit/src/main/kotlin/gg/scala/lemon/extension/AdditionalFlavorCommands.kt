package gg.scala.lemon.extension

import gg.scala.flavor.Flavor
import gg.scala.flavor.service.Service
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author GrowlyX
 * @since 1/4/2022
 */
class AdditionalFlavorCommands(
    private val flavor: Flavor,
    private val plugin: JavaPlugin
) : BaseCommand()
{
    @CommandAlias("lemon-services")
    @CommandPermission("flavor.commands")
    @Description("View all enabled services.")
    fun onDefault(sender: CommandSender)
    {
        val services = flavor.services
        sender.sendMessage("${CC.SEC}Loaded services ${CC.GRAY}(${services.size})${CC.SEC}: ${CC.PRI}${
            services.values
                .map { it.javaClass.getAnnotation(Service::class.java) to it }
                .joinToString(
                    separator = "${CC.SEC}, ${CC.PRI}"
                ) { 
                    it.first.name.ifBlank { 
                        it.second.javaClass.simpleName 
                    } 
                }
        }")
    }
}