package ootbingo.barinade.bot.racing_services.racetime.sync

import com.nhaarman.mockitokotlin2.*
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceResultRepository
import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

internal class RacetimeImporterTest {

  private val playerHelperMock = mock<PlayerHelper>()
  private val raceRepositoryMock = mock<RaceRepository>()
  private val raceResultRepositoryMock = mock<RaceResultRepository>()

  @Test
  internal fun doesNotOverwriteRace() {

    val raceId = UUID.randomUUID().toString()

    givenRaceWithId(raceId).isSavedInDb()

    whenRaceIsImported(randomBingoWithId(raceId))

    thenNoRaceWasSaved()
    thenNoResultWasSaved()
  }

  //<editor-fold desc="Given">

  private fun givenRaceWithId(raceId: String) = Race().apply {
    this.raceId = raceId
    this.platform = Platform.RACETIME
  }

  private fun Race.isSavedInDb() {
    whenever(raceRepositoryMock.findByRaceId(this.raceId)).thenReturn(this)
    whenever(raceRepositoryMock.findAll()).thenReturn(setOf(this))
    whenever(raceRepositoryMock.findAllByPlatform(Platform.RACETIME)).thenReturn(setOf(this))
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenRaceIsImported(race: RacetimeRace) =
      RacetimeImporter(playerHelperMock, raceRepositoryMock, raceResultRepositoryMock).import(setOf(race))

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenNoRaceWasSaved() = verify(raceRepositoryMock, never()).save(any<Race>())

  private fun thenNoResultWasSaved() = verify(raceResultRepositoryMock, never()).save(any<RaceResult>())

  //</editor-fold>

  //<editor-fold desc="Helper">

  private fun randomBingoWithId(raceId: String) =
      RacetimeRace(name = raceId, goal = RacetimeRace.RacetimeRaceGoal("Bingo", false), endedAt = Instant.now())

  //</editor-fold>
}
