package ootbingo.barinade.bot.extensions

import ootbingo.barinade.bot.balancing.Team
import ootbingo.barinade.bot.balancing.TeamMember

fun <T> Collection<T>.allPartitions(maxSize: Int = 4): List<List<List<T>>> {

  require(maxSize <= 4) { "Only partitions with a size up to four can be computed." }
  require(this.size <= 12) { "Only partitions for up to 12 elements can be computed." }

  val result = ArrayList<List<List<T>>>()

  if (this.size == 1) {
    return mutableListOf(listOf(this.toList()))
  }

  val baseElement = this.first()
  for (otherElements in this.minus(baseElement).allPartitions(maxSize)) {

    for (n in otherElements.indices) {
      val partition = otherElements.slice(0 until n) +
          listOf(listOf(baseElement) + otherElements[n]) +
          otherElements.slice((n + 1) until otherElements.size)

      if (partition.none { it.size > maxSize }) {
        result.add(partition)
      }
    }

    val partition = listOf(listOf(baseElement)) + otherElements

    if (partition.none { it.size > maxSize }) {
      result.add(partition)
    }
  }

  return result
}

fun List<TeamMember>.allTeamPartitions(maxSize: Int): List<List<Team>> {

  return this.allPartitions(maxSize)
      .map { it.map { members -> Team(members) } }
}
