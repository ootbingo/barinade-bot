package ootbingo.barinade.bot.racing_services.data

import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceResultRepository
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.random.Random

@DataJpaTest
internal class DbIntegrationTest(
    @Autowired val playerRepository: PlayerRepository,
    @Autowired val raceRepository: RaceRepository,
    @Autowired val raceResultRepository: RaceResultRepository,
) {

  @Test
  @DirtiesContext
  internal fun saveAndFindPlayer() {

    val srlId = Random.nextLong(0, 10000)
    val playerName = UUID.randomUUID().toString()

    playerRepository.save(Player(null, srlId, null, playerName, null, mutableListOf()))

    val actualPlayer = playerRepository.findBySrlNameIgnoreCase(playerName.toUpperCase())

    assertThat(actualPlayer!!.srlName).isEqualTo(playerName)
    assertThat(actualPlayer.srlId).isEqualTo(srlId)
  }

  @Test
  @DirtiesContext
  internal fun navigateBetweenObjects() {

    val srlName = "player"

    val player = Player(null, 0, null, srlName)
    val savedPlayer = playerRepository.save(player)

    val race = Race("123", "", Instant.now())
    val savedRace = raceRepository.save(race)

    val result = RaceResult(RaceResult.ResultId(savedRace, savedPlayer), 1, Duration.ofSeconds(12))
    val savedRaceResult = raceResultRepository.save(result)

    savedPlayer.raceResults.add(savedRaceResult)
    savedRace.raceResults.add(savedRaceResult)
    playerRepository.save(savedPlayer)
    raceRepository.save(savedRace)

    val actualPlayer = playerRepository.findBySrlNameIgnoreCase(srlName)!!
    val actualResults = actualPlayer.raceResults
    val actualRaces = actualPlayer.races

    assertThat(actualResults.size).isEqualTo(1)
    assertThat(actualRaces.size).isEqualTo(1)

    assertThat(actualResults[0].resultId.race).isEqualTo(actualRaces[0])
    assertThat(actualRaces[0]).isEqualTo(actualPlayer.races[0])
    assertThat(actualRaces[0].raceResults[0].resultId.player).isEqualTo(actualPlayer)
  }
}
