package gg.scala.lemon.adapter.statistic

interface ServerStatisticProvider
{
    fun ticksPerSecond(): Double
    fun cpuUsage(): Double
    fun memoryUsage(): Long
}
