package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class EntrantPairGenerator {

  fun generatePairs(entrants: List<RacetimeUser>): List<AntiBingoState.EntrantMapping> {

    if (entrants.size < 2) throw IllegalArgumentException("Entrant list with size ${entrants.size} cannot be mapped")

    val randomizedEntrants = entrants.shuffled(Random(System.currentTimeMillis()))

    return buildList(randomizedEntrants.size) {
      randomizedEntrants.forEachIndexed { i, _ ->
        if (i == 0) add(AntiBingoState.EntrantMapping(randomizedEntrants.last(), randomizedEntrants[0], null))
        else add(AntiBingoState.EntrantMapping(randomizedEntrants[i - 1], randomizedEntrants[i], null))
      }
    }
  }
}
