package ootbingo.barinade.bot.balancing

import ootbingo.barinade.bot.compile.Open
import ootbingo.barinade.bot.extensions.standardFormat
import java.time.Duration
import kotlin.math.roundToLong

@Open
class Team(val members: List<TeamMember>) {

  val predictedTime: Duration = members
      .mapNotNull { it.workRate }
      .sum()
      .let { 1 / it }
      .let { it * blackoutRatio }
      .let { it + setupTime + overlap }
      .roundToLong()
      .let { Duration.ofSeconds(it) }

  override fun toString(): String {
    return members
        .joinToString(", ") { it.name }
        .let { "$it (${predictedTime.standardFormat()})" }
  }
}
