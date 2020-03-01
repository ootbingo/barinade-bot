package ootbingo.barinade.bot.data.connection

import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.data.model.RaceResult
import ootbingo.barinade.bot.data.model.helper.ResultInfo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

@DataJpaTest
internal class PlayerRepositoryIntegrationTest(@Autowired private val playerRepository: PlayerRepository,
                                               @Autowired private val raceRepository: RaceRepository) {

  @Test
  @DirtiesContext
  internal fun findsResultsForPlayer() {

    val raceGoals = mutableListOf<String>()

    repeat((1..5).count()) {
      raceGoals.add(UUID.randomUUID().toString())
    }

    val player = playerRepository.save(Player(0, "name", mutableListOf()))

    val savedRaces = raceGoals.map {
      Race("${Random.nextLong()}",
           it,
           ZonedDateTime.now().plusSeconds(Random.nextLong(0, 10000)),
           1,
           mutableListOf())
    }.map {
      val race = raceRepository.save(it)
      race.raceResults.add(RaceResult(race = race, player = player, place = 1,
                                      time = Duration.ofSeconds(Random.nextLong(0, 7000))))
      raceRepository.save(race)
    }

    val expectedRaces = savedRaces
        .asSequence()
        .sortedByDescending { it.recordDate }
        .map {
          ResultInfo(it.raceResults[0].time, it.goal, it.srlId, it.recordDate)
        }
        .toList()

    val actualResults = playerRepository.findResultsForPlayer("name")

    assertThat(actualResults).containsExactlyElementsOf(expectedRaces)
  }
}
