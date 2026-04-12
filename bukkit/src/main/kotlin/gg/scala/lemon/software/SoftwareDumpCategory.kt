package gg.scala.lemon.software

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
