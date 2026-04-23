package gg.scala.lemon.software

import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage

/**
 * @author GrowlyX
 * @since 1/1/2022
 */
class SoftwareDump(
    val identifier: String
)
{
    val categories = mutableListOf<SoftwareDumpCategory>()

    fun addCategory(category: SoftwareDumpCategory)
    {
        categories.add(category)
    }

    fun formFancyMessage(): FancyMessage
    {
        val fancyMessage = FancyMessage()
        fancyMessage.withMessage(
            "${CC.PRI}=== ${CC.SEC}$identifier ${CC.PRI}===\n"
        )

        for (category in categories)
        {
            fancyMessage.withMessage(
                " ${CC.B_PRI}${category.identifier}\n"
            )

            for (entry in category.entries)
            {
                fancyMessage.withMessage(
                    "  ${CC.GRAY}● ${CC.SEC}${
                        entry.first
                    }: ${CC.WHITE}${
                        entry.second
                    }"
                )
                fancyMessage.withMessage("\n")
            }

            fancyMessage.withMessage("\n")
        }

        fancyMessage.components
            .removeLast()
        fancyMessage.components
            .removeLast()

        return fancyMessage
    }
}
