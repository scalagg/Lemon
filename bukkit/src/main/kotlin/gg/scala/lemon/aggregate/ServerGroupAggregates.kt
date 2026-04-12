package gg.scala.lemon.aggregate

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object ServerGroupAggregates : ScheduledExecutorService by Executors.newSingleThreadScheduledExecutor()
