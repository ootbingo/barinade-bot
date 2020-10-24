package ootbingo.barinade.bot.racing_services.racetime.sync

import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceResultRepository
import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant.RacetimeEntrantStatus.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Duration
import java.time.Instant
import java.util.UUID

@DataJpaTest
internal class RacetimeImporterIntegrationTest(@Autowired private val playerRepository: PlayerRepository,
                                               @Autowired private val raceRepository: RaceRepository,
                                               @Autowired private val raceResultRepository: RaceResultRepository) {

  private val importer = RacetimeImporter(PlayerHelper(playerRepository), raceRepository, raceResultRepository)

  @Test
  internal fun importsSingleNewBingoRaceWithNewPlayers() {

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

  @Test
  internal fun importsSingleNewBingoRaceWithExistingPlayers() {

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

    givenPlayersInDb(Player(racetimeId = user1.first, racetimeName = user1.second),
                     Player(racetimeId = user2.first, racetimeName = user2.second))

    whenRaceIsImported(race)

    thenUser(user1.first) hasRacetimeName user1.second
    thenUser(user2.first) hasRacetimeName user2.second

    thenRace(raceId) hasGoal raceInfo
    thenRace(raceId) hasDatetime date

    thenRace(raceId) hasFinishTimes listOf(null, 3600)
    thenRace(raceId) hasResultTypes listOf(ResultType.FORFEIT, ResultType.FINISH)

    thenRace(raceId) hasPlatform Platform.RACETIME
  }

  @Test
  internal fun doesNotImportRaceWithCustomGoal() {

    val raceId = UUID.randomUUID().toString()
    val race = randomRace(raceId, RacetimeRace.RacetimeRaceGoal("Bingo", true))

    whenRaceIsImported(race)

    thenRace(raceId).doesNotExistInDb()
  }

  @Test
  internal fun doesNotImportNonBingoRace() {

    val raceId = UUID.randomUUID().toString()
    val race = randomRace(raceId, RacetimeRace.RacetimeRaceGoal("Glitchless", false))

    whenRaceIsImported(race)

    thenRace(raceId).doesNotExistInDb()
  }

  @Test
  internal fun importsMultipleRaces() {

    val raceId1 = UUID.randomUUID().toString()
    val raceId2 = UUID.randomUUID().toString()
    val raceInfo = UUID.randomUUID().toString()
    val date = Instant.ofEpochSecond(1234567)

    val user1 = UUID.randomUUID().toString() to UUID.randomUUID().toString()
    val user2 = UUID.randomUUID().toString() to UUID.randomUUID().toString()

    val races = setOf(raceId1, raceId2)
        .map {
          RacetimeRace().apply {
            name = it
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
        }

    whenRacesAreImported(races)

    thenUser(user1.first) hasRacetimeName user1.second
    thenUser(user2.first) hasRacetimeName user2.second

    thenRace(raceId1) hasGoal raceInfo
    thenRace(raceId1) hasDatetime date

    thenRace(raceId1) hasFinishTimes listOf(null, 3600)
    thenRace(raceId1) hasResultTypes listOf(ResultType.FORFEIT, ResultType.FINISH)

    thenRace(raceId1) hasPlatform Platform.RACETIME

    thenRace(raceId2) hasGoal raceInfo
    thenRace(raceId2) hasDatetime date

    thenRace(raceId2) hasFinishTimes listOf(null, 3600)
    thenRace(raceId2) hasResultTypes listOf(ResultType.FORFEIT, ResultType.FINISH)

    thenRace(raceId2) hasPlatform Platform.RACETIME
  }

  //<editor-fold desc="Given">

  fun givenPlayersInDb(vararg players: Player) =
      players.forEach { playerRepository.save(it) }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenRaceIsImported(race: RacetimeRace) {
    importer.import(setOf(race))
  }

  private fun whenRacesAreImported(races: Collection<RacetimeRace>) {
    importer.import(races)
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

  private fun Race?.doesNotExistInDb() =
      assertThat(this).isNull()

  //</editor-fold>

  //<editor-fold desc="Helper">

  fun randomRace(raceId: String, goal: RacetimeRace.RacetimeRaceGoal): RacetimeRace {

    val raceInfo = UUID.randomUUID().toString()
    val date = Instant.ofEpochSecond(98765)

    val user1 = UUID.randomUUID().toString() to UUID.randomUUID().toString()
    val user2 = UUID.randomUUID().toString() to UUID.randomUUID().toString()

    return RacetimeRace().apply {
      name = raceId
      this.goal = goal
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
  }

  //</editor-fold>
}
