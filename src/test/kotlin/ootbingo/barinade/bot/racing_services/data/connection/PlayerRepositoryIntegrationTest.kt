package ootbingo.barinade.bot.racing_services.data.connection

import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class PlayerRepositoryIntegrationTest(
    @Autowired private val playerRepository: PlayerRepository,
    @Autowired private val raceRepository: RaceRepository,
) {

  @Test
  @DirtiesContext
  internal fun findsResultsForPlayer() {

    val raceGoals = mutableListOf<String>()

    repeat((1..5).count()) {
      raceGoals.add(UUID.randomUUID().toString())
    }

    val player = playerRepository.save(Player(null, 0, null, "name", null, mutableListOf()))

    val savedRaces = raceGoals.map {
      Race("${Random.nextLong()}",
          it,
          Instant.now().plusSeconds(Random.nextLong(0, 10000)).truncatedTo(ChronoUnit.MICROS),
          Platform.SRL,
          mutableListOf())
    }.map {
      val race = raceRepository.save(it)
      race.raceResults.add(RaceResult(RaceResult.ResultId(race, player), place = 1,
          time = Duration.ofSeconds(Random.nextLong(0, 7000))))
      raceRepository.save(race)
    }

    val expectedRaces = savedRaces
        .asSequence()
        .sortedByDescending { it.datetime }
        .map {
          ResultInfo(it.raceResults[0].time, it.goal, it.raceId, it.datetime, it.raceResults[0].resultType)
        }
        .toList()

    val actualResults = playerRepository.findResultsForPlayer(player)

    assertThat(actualResults).containsExactlyElementsOf(expectedRaces)
  }
}
