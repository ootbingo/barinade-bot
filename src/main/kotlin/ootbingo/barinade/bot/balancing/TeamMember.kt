package ootbingo.barinade.bot.balancing

import kotlin.math.min

class TeamMember(val name: String, median: Long?, forfeitRatio: Double?) {

  val workRate: Double? = median
      ?.let { it * (1 + 2 * min(forfeitRatio!!, 0.5) * (forfeitThresholdFactor - 1)) }
      ?.let { (1 / (it - setupTime)) }
}
