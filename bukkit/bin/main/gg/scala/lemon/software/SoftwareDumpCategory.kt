package gg.scala.lemon.software

/**
 * @author GrowlyX
 * @since 1/1/2022
 */
class SoftwareDumpCategory(
    val identifier: String
)
{
    val entries = mutableListOf<Pair<String, Any>>()

    fun addEntry(pair: Pair<String, Any>)
    {
        entries.add(pair)
    }
}
