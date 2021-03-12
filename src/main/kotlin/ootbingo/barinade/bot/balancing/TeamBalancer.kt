package ootbingo.barinade.bot.balancing

import org.springframework.stereotype.Component

@Component
class TeamBalancer {

  fun findBestTeamBalance(teamPartitions: List<List<Team>>): List<Team> {

    lateinit var result: List<Team>
    var smallestDifference = Long.MAX_VALUE

    for (partition in teamPartitions) {
      val difference = partition.map { it.predictedTime.toSeconds() }.let { it.maxOrNull()!! - it.minOrNull()!! }
      if (difference < smallestDifference) {
        smallestDifference = difference
        result = partition
      }
    }

    return result
  }
}
