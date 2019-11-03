package ootbingo.barinade.bot.balancing

import java.time.Duration
import kotlin.math.roundToLong

class Team(val members: List<TeamMember>) {

  val predictedTime = members
      .mapNotNull { it.workRate }
      .sum()
      .let { 1 / it }
      .let { it * blackoutRatio }
      .let { it + setupTime + overlap }
      .roundToLong()
      .let { Duration.ofSeconds(it) }
}
