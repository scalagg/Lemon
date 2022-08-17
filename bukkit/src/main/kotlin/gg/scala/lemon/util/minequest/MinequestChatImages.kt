package gg.scala.lemon.util.minequest

import net.evilblock.cubed.util.nms.MinecraftProtocol
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/25/2022
 */
object MinequestChatImages
{
    fun triviaImage(player: Player): List<String>
    {
        val protocol = MinecraftProtocol
            .getPlayerVersion(player)

        return if (protocol <= 393)
        {
            listOf(
                "" // TODO
            )
        } else
        {
            listOf("ŉ")
        }
    }

    fun eventImage(player: Player): List<String>
    {
        val protocol = MinecraftProtocol
            .getPlayerVersion(player)

        return if (protocol <= 393)
        {
            listOf(
                "" // TODO
            )
        } else
        {
            listOf("ŋ")
        }
    }

    fun levelUpBlueImage(player: Player): List<String>
    {
        val protocol = MinecraftProtocol
            .getPlayerVersion(player)

        return if (protocol <= 393)
        {
            listOf(
                "" // TODO
            )
        } else
        {
            listOf("ł")
        }
    }

    fun levelUpGrayImage(player: Player): List<String>
    {
        val protocol = MinecraftProtocol
            .getPlayerVersion(player)

        return if (protocol <= 393)
        {
            listOf(
                "" // TODO
            )
        } else
        {
            listOf("Ł")
        }
    }

    fun thankYouImage(player: Player): List<String>
    {
        val protocol = MinecraftProtocol
            .getPlayerVersion(player)

        return if (protocol <= 393)
        {
            listOf(
                "" // TODO
            )
        } else
        {
            listOf("Ŋ")
        }
    }

    fun questStartedImage(player: Player): List<String>
    {
        val protocol = MinecraftProtocol
            .getPlayerVersion(player)

        return if (protocol <= 393)
        {
            listOf(
                "䲰㼆䲱㼆䲲㼆䲳㼆䲴㼆䲵㼆䲶㼆䲷㼆䲸㼆䲹㼆䲺㼆䲺㼆䲺㼆䲺㼆䲺㼆䲺㼆䱠㼆䱡㼆䱢㼆䱣㼆䱤㼆䱥㼆䱦㼆䱧㼆䱨㼆䱩㼆䱪㼆䱫㼆䱬㼆䱭",
                "䳀㼆䳁㼆䳂㼆䳃㼆䳄㼆䳅㼆䳆㼆䳇㼆䳈㼆䳉㼆䳊㼆䳋㼆䳌㼆䳍㼆䳎㼆䳏㼆䱰㼆䱱㼆䱲㼆䱳㼆䱴㼆䱵㼆䱶㼆䱷㼆䱸㼆䱹㼆䱺㼆䱻㼆䱼㼆䱽",
                "䳐㼆䳑㼆䳒㼆䳓㼆䳔㼆䳕㼆䳖㼆䳗㼆䳘㼆䳙㼆䳚㼆䳛㼆䳜㼆䳝㼆䳞㼆䳟㼆䲀㼆䲁㼆䲂㼆䲃㼆䲄㼆䲅㼆䲆㼆䲇㼆䲈㼆䲉㼆䲊㼆䲋㼆䲌㼆䲍",
                "䳠㼆䳡㼆䳢㼆䳣㼆䳤㼆䳥㼆䳦㼆䳧㼆䳨㼆䳩㼆䳪㼆䳫㼆䳬㼆䳭㼆䳮㼆䳯㼆䲐㼆䲑㼆䲒㼆䲓㼆䲔㼆䲕㼆䲖㼆䲗㼆䲘㼆䲙㼆䲚㼆䲛㼆䲜㼆䲝",
                "䳰㼆䳱㼆䳲㼆䳳㼆䳴㼆䳵㼆䳶㼆䳷㼆䳸㼆䳹㼆䳺㼆䳻㼆䳼㼆䳽㼆䳾㼆䳿㼆䲠㼆䲡㼆䲢㼆䲣㼆䲤㼆䲥㼆䲦㼆䲧㼆䲨㼆䲩㼆䲪㼆䲫㼆䲬㼆䲭"
            )
        } else
        {
            listOf("Š")
        }
    }

    fun questCompleteImage(player: Player): List<String>
    {
        val protocol = MinecraftProtocol
            .getPlayerVersion(player)

        return if (protocol <= 393)
        {
            listOf(
                "趰㼆趱㼆趲㼆足㼆趴㼆趵㼆趶㼆趷㼆趸㼆趹㼆趺㼆趻㼆趼㼆趽㼆趾㼆趿㼆赠㼆赡㼆赢㼆赣㼆赤㼆赥㼆赦㼆赧㼆赨㼆赩㼆赪㼆赫㼆赬㼆赭",
                "跀㼆跁㼆跂㼆跃㼆跄㼆跅㼆跆㼆跇㼆跈㼆跉㼆跊㼆跋㼆跌㼆跍㼆跎㼆跏㼆走㼆赱㼆赲㼆赳㼆赴㼆赵㼆赶㼆起㼆赸㼆赹㼆赺㼆赻㼆赼㼆赽",
                "跐㼆跑㼆跒㼆跓㼆跔㼆跕㼆跖㼆跗㼆跘㼆跙㼆跚㼆跛㼆跜㼆距㼆跞㼆跟㼆趀㼆趁㼆趂㼆趃㼆趄㼆超㼆趆㼆趇㼆趈㼆趉㼆越㼆趋㼆趌㼆趍",
                "跠㼆跡㼆跢㼆跣㼆跤㼆跥㼆跦㼆跧㼆跨㼆跩㼆跪㼆跫㼆跬㼆跭㼆跮㼆路㼆趐㼆趑㼆趒㼆趓㼆趔㼆趕㼆趖㼆趗㼆趘㼆趙㼆趚㼆趛㼆趜㼆趝",
                "跰㼆跱㼆跲㼆跳㼆跴㼆践㼆跶㼆跷㼆跸㼆跹㼆跺㼆跻㼆跼㼆跽㼆跾㼆跿㼆趠㼆趡㼆趢㼆趣㼆趤㼆趥㼆趦㼆趧㼆趨㼆趩㼆趪㼆趫㼆趬㼆趭"
            )
        } else
        {
            listOf("Λ")
        }
    }
}
