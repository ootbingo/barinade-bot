package ootbingo.barinade.bot.racing_services.srl.sync

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.srl.api.client.SrlHttpClient
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlGame
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.racing_services.srl.api.model.SrlResult
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Duration
import java.util.UUID

internal class SrlImporterTest {

  private val srlHttpClientMock = mock(SrlHttpClient::class.java)
  private val raceRepositoryMock = mock(RaceRepository::class.java)

  private val importer = SrlImporter(srlHttpClientMock, raceRepositoryMock)

  private val oot = SrlGame(1, "Ocarina of Time", "oot", 1.0, 1)
  private val ootbingo = SrlGame(2, "Ocarina of Time Bingo", "ootbingo", 1.0, 2)
  private val otherGame = SrlGame(3, "Super Mario 64", "sm64", 0.0, 3)

  @BeforeEach
  internal fun setup() {
    givenGamesOnSrl(oot, ootbingo, otherGame)
    doAnswer { it.getArgument(0) }.`when`(raceRepositoryMock).save(any<Race>())
  }

  @Test
  internal fun savesAllOotRacesOfPlayer() {

    val playerName = UUID.randomUUID().toString()
    val races = listOf(SrlPastRace("1", oot),
                       SrlPastRace("2", oot),
                       SrlPastRace("3", ootbingo))

    givenRacesForPlayer(playerName, *races.toTypedArray())

    importer.importRacesForUser(Player(null, 0, null, playerName))

    thenRacesWithIdsAreSaved(1, 2, 3)
  }

  @Test
  internal fun doesNotSaveRacesOfOtherGames() {

    val playerName = UUID.randomUUID().toString()
    val races = listOf(SrlPastRace("1", oot),
                       SrlPastRace("2", otherGame),
                       SrlPastRace("3", ootbingo))

    givenRacesForPlayer(playerName, *races.toTypedArray())

    importer.importRacesForUser(Player(null, 0, null, playerName))

    thenRacesWithIdsAreSaved(1, 3)
  }

  @Test
  internal fun savesTimeOfPlayer() {

    val playerName = UUID.randomUUID().toString()

    val result = SrlResult(player = playerName, place = 42)
    val races = listOf(SrlPastRace("1", oot, results = listOf(result)))

    givenRacesForPlayer(playerName, *races.toTypedArray())

    importer.importRacesForUser(Player(null, 0, null, playerName))

    thenResultIsSaved(playerName, races[0], 42)
  }

  @Test
  internal fun storeAllOtherResultsIfOneIsMissing() {

    val playerName = UUID.randomUUID().toString()

    val races = listOf(SrlPastRace("1", oot, results = listOf(SrlResult(player = playerName, place = 42))),
                       SrlPastRace("2", oot, results = listOf(SrlResult(player = playerName + "x", place = 43))),
                       SrlPastRace("3", oot, results = listOf(SrlResult(player = playerName, place = 44))))

    givenRacesForPlayer(playerName, *races.toTypedArray())

    importer.importRacesForUser(Player(null, 0, null, playerName))

    thenResultIsSaved(playerName, races[0], 42)
    thenResultIsSaved(playerName, races[2], 44)
  }

  @Test
  internal fun doesNotSaveWhenAResultForPlayerExists() {

    val playerName = UUID.randomUUID().toString()

    val result = SrlResult(player = playerName, place = 42)
    val races = listOf(SrlPastRace("1", oot, results = listOf(result)))

    givenRacesForPlayer(playerName, *races.toTypedArray())
    givenRacesInDb(
        Race("1", raceResults = mutableListOf(RaceResult(RaceResult.ResultId(player = Player(srlName = playerName)), place = 42))))

    importer.importRacesForUser(Player(null, 0, null, playerName))

    verify(raceRepositoryMock, times(0)).save(any<Race>())
    verify(raceRepositoryMock, times(0)).saveAll(any<Iterable<Race>>())
  }

  @Test
  internal fun importsWithTheCorrectResultType() {

    val name = UUID.randomUUID().toString()

    val races = listOf(
        SrlPastRace("1", oot, results = listOf(SrlResult(player = name, place = 42, time = Duration.ofSeconds(1)))),
        SrlPastRace("2", oot, results = listOf(SrlResult(player = name, place = 42, time = Duration.ofSeconds(2)))),
        SrlPastRace("3", oot, results = listOf(SrlResult(player = name, place = 42, time = Duration.ofSeconds(-1)))),
        SrlPastRace("4", oot, results = listOf(SrlResult(player = name, place = 42, time = Duration.ofSeconds(-2))))
    )

    givenRacesForPlayer(name, *races.toTypedArray())
    givenRacesInDb()

    importer.importRacesForUser(Player(null, 0, null, name))

    thenResultTypesAre("1" to ResultType.FINISH, "2" to ResultType.FINISH,
                       "3" to ResultType.FORFEIT, "4" to ResultType.FORFEIT)
  }

  private fun thenResultTypesAre(vararg results: Pair<String, ResultType>) {

    val raceCaptor = argumentCaptor<Race>()
    verify(raceRepositoryMock, atLeast(1)).save(raceCaptor.capture())

    val races = raceCaptor.allValues

    results.forEach {
      assertThat(races.last { r->r.raceId == it.first }.raceResults[0].resultType).isEqualTo(it.second)
    }
  }

  private fun givenRacesInDb(vararg races: Race) {

    doAnswer {
      races.lastOrNull { r -> r.raceId == it.getArgument(0) }
    }.`when`(raceRepositoryMock).findByRaceId(anyString())
  }

  private fun thenResultIsSaved(player: String, race: SrlPastRace, place: Long) {

    val raceCaptor = argumentCaptor<Race>()
    verify(raceRepositoryMock, atLeast(1)).save(raceCaptor.capture())

    val lastSavedResult = raceCaptor
        .allValues
        .findLast { it.raceId == race.id }
        ?.let { it.raceResults.findLast { r -> r.resultId.player.srlName == player } }

    assertThat(lastSavedResult?.place).isEqualTo(place)
  }

  private fun thenRacesWithIdsAreSaved(vararg ids: Long) {

    val raceCaptor = argumentCaptor<Race>()
    verify(raceRepositoryMock, atLeast(1)).save(raceCaptor.capture())

    val capturedIds = raceCaptor
        .allValues
        .map { it.raceId.toLong() }
        .distinct()

    assertThat(capturedIds).containsExactlyInAnyOrder(*ids.toTypedArray())
  }

  private fun givenRacesForPlayer(player: String, vararg races: SrlPastRace) {
    `when`(srlHttpClientMock.getRacesByPlayerName(player)).thenReturn(races.toList())
  }

  private fun givenGamesOnSrl(vararg games: SrlGame) {
    doAnswer {
      games
          .findLast { g -> g.abbrev == it.getArgument(0) }
    }.`when`(srlHttpClientMock).getGameByAbbreviation(anyString())
  }
}
