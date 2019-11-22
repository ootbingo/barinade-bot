package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.connection.RaceResultRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.data.model.RaceResult
import ootbingo.barinade.bot.testutils.DbUtils
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

@DataJpaTest
@ExtendWith(SpringExtension::class)
internal class DbIntegrationTest(@Autowired val playerRepository: PlayerRepository,
                                 @Autowired val raceRepository: RaceRepository,
                                 @Autowired val raceResultRepository: RaceResultRepository,
                                 @Autowired val dbUtils: DbUtils) {

  @Test
  @DirtiesContext
  internal fun saveAndFindPlayer() {

    val srlId = Random.nextLong(0, 10000)
    val playerName = UUID.randomUUID().toString()

    playerRepository.save(Player(srlId, playerName, mutableListOf()))

    val actualPlayer = playerRepository.findBySrlNameIgnoreCase(playerName.toUpperCase())

    assertThat(actualPlayer!!.srlName).isEqualTo(playerName)
    assertThat(actualPlayer.srlId).isEqualTo(srlId)
  }

  @Test
  internal fun navigateBetweenObjects() {

    val srlName = "player"


    val player = Player(0, srlName)
    val savedPlayer =playerRepository.save(player)

    val race = Race("123", "", ZonedDateTime.now(), 1, mutableListOf())
    val savedRace = raceRepository.save(race)

    val result = RaceResult(123, savedRace, savedPlayer, 1, Duration.ofSeconds(12), "test")
    val savedRaceResult = raceResultRepository.save(result)

    savedPlayer.raceResults.add(savedRaceResult)
    savedRace.raceResults.add(savedRaceResult)
    playerRepository.save(savedPlayer)
    raceRepository.save(savedRace)


    val actualPlayer = playerRepository.findBySrlNameIgnoreCase(srlName)!!
    val actualResults = actualPlayer.raceResults
    val actualRaces = actualPlayer.races

    val test = raceResultRepository.findAll()

    assertThat(actualResults.size).isEqualTo(1)
    assertThat(actualRaces.size).isEqualTo(1)

    assertThat(actualResults[0].race).isEqualTo(actualRaces[0])
    assertThat(actualRaces[0]).isEqualTo(actualPlayer.races[0])
    assertThat(actualRaces[0].raceResults[0].player).isEqualTo(actualPlayer)
  }
}
