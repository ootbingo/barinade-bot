package ootbingo.barinade.bot.racetime.sync

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.connection.RaceResultRepository
import ootbingo.barinade.bot.data.model.Platform
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.data.model.ResultType
import ootbingo.barinade.bot.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racetime.api.model.RacetimeEntrant.RacetimeEntrantStatus.*
import ootbingo.barinade.bot.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racetime.api.model.RacetimeUser
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Duration
import java.time.Instant
import java.util.UUID

@DataJpaTest
internal class RacetimeImporterTest(@Autowired private val playerRepository: PlayerRepository,
                                    @Autowired private val raceRepository: RaceRepository,
                                    @Autowired private val raceResultRepository: RaceResultRepository) {

  private val importer = RacetimeImporter(playerRepository, raceRepository, raceResultRepository)

  @Test
  internal fun importsSingleBingoRaceWithNewPlayers() {

    val raceId = UUID.randomUUID().toString()
    val raceInfo = UUID.randomUUID().toString()
    val date = Instant.ofEpochSecond(1234567)

    val user1 = UUID.randomUUID().toString() to UUID.randomUUID().toString()
    val user2 = UUID.randomUUID().toString() to UUID.randomUUID().toString()

    val race = RacetimeRace().apply {
      name = raceId
      goal = RacetimeRace.RacetimeRaceGoal("Bingo", false)
      info = raceInfo
      endedAt = date
      recorded = true
      entrants = listOf(
          RacetimeEntrant().apply {
            user = RacetimeUser(user1.first, user1.second)
            finishTime = Duration.ofHours(1)
            place = 1
            status = DONE
          },
          RacetimeEntrant().apply {
            user = RacetimeUser(user2.first, user2.second)
            finishTime = null
            place = null
            status = DNF
          }
      )
    }


    whenRaceIsImported(race)

    thenUser(user1.first) hasRacetimeName user1.second
    thenUser(user2.first) hasRacetimeName user2.second

    thenRace(raceId) hasGoal raceInfo
    thenRace(raceId) hasDatetime date

    thenRace(raceId) hasFinishTimes listOf(null, 3600)
    thenRace(raceId) hasResultTypes listOf(ResultType.FORFEIT, ResultType.FINISH)

    thenRace(raceId) hasPlatform Platform.RACETIME
  }

  //<editor-fold desc="When">

  private fun whenRaceIsImported(race: RacetimeRace) {
    importer.import(race)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenUser(racetimeUserId: String) = playerRepository.findByRacetimeId(racetimeUserId)

  private infix fun Player?.hasRacetimeName(expectedRacetimeName: String) =
      assertThat(this?.racetimeName).isEqualTo(expectedRacetimeName)

  private fun thenRace(raceId: String) = raceRepository.findByRaceId(raceId)

  private infix fun Race?.hasGoal(expectedGoal: String) =
      assertThat(this?.goal).isEqualTo(expectedGoal)

  private infix fun Race?.hasDatetime(expectedDatetime: Instant) =
      assertThat(this?.datetime).isEqualTo(expectedDatetime)

  private infix fun Race?.hasFinishTimes(expectedTimes: List<Long?>) =
      assertThat(raceResultRepository
                     .findAll()
                     .filter { it.resultId.race == this }
                     .toMutableList()
                     .also { r -> r.sortBy { it.place } }
                     .map { it.time }
      ).containsExactlyElementsOf(expectedTimes.map { it?.let { s -> Duration.ofSeconds(s) } })

  private infix fun Race?.hasResultTypes(expectedResultTypes: List<ResultType>) =
      assertThat(raceResultRepository
                     .findAll()
                     .filter { it.resultId.race == this }
                     .toMutableList()
                     .also { r -> r.sortBy { it.place } }
                     .map { it.resultType }
      ).containsExactlyElementsOf(expectedResultTypes)

  private infix fun Race?.hasPlatform(expectedPlatform: Platform) =
      assertThat(this?.platform).isEqualTo(expectedPlatform)

  //</editor-fold>
}
