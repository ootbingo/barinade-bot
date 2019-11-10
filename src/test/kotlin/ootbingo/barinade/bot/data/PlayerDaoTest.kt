package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import ootbingo.barinade.bot.srl.api.model.SrlGame
import ootbingo.barinade.bot.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import ootbingo.barinade.bot.srl.api.model.SrlResult
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random

internal class PlayerDaoTest {

  private val srlHttpClientMock = mock(SrlHttpClient::class.java)
  private val clockMock = mock(Clock::class.java)
  private lateinit var dao: PlayerDao

  private val allGames = HashMap<String, SrlGame>()

  @BeforeEach
  internal fun setup() {
    `when`(clockMock.instant()).thenReturn(Instant.ofEpochSecond(0))
    dao = PlayerDao(srlHttpClientMock, clockMock)
  }

  @Test
  internal fun getAllOotRacesForPlayer() {

    val playerName = UUID.randomUUID().toString()

    givenPlayers(SrlPlayer(0, playerName, "", "", "", "", ""),
                 SrlPlayer(1, "other player", "", "", "", "", ""))

    givenGames(SrlGame(1, "OoT", "oot", 0.0, 0),
               SrlGame(2, "Bingo", "ootbingo", 0.0, 0),
               SrlGame(3, "Other", "sm64", 0.0, 0))

    givenRaces(race("oot", time(0), result(1, playerName, 123),
                    result(2, "other player", 127)),
               race("oot", time(37), result(1, playerName, 124),
                    result(2, "other player", 127)),
               race("ootbingo", time(100), result(1, playerName, 345),
                    result(2, "other player", 999)),
               race("ootbingo", time(256), result(1, "other player", 1)),
               race("sm64", time(1909), result(1, playerName, 666),
                    result(2, "other player", 1000)))

    val player = dao.getPlayerByName(playerName)

    assertThat(player?.races
                   ?.map { requireNotNull(it.raceResults.findLast { result -> result.player.srlName == playerName }) }
                   ?.map { it.time }
                   ?.map { it.seconds })
        .containsExactly(123, 124, 345)
  }

  @Test
  internal fun returnsNullIfPlayerNotKnown() {

    givenPlayers()

    assertThat(dao.getPlayerByName("some name")).isNull()
  }

  @Test
  internal fun mapsRaceToResult() {

    val playerName = UUID.randomUUID().toString()

    givenPlayers(SrlPlayer(0, playerName, "", "", "", "", ""),
                 SrlPlayer(1, "other player", "", "", "", "", ""))

    givenGames(SrlGame(1, "OoT", "oot", 0.0, 0),
               SrlGame(2, "Bingo", "ootbingo", 0.0, 0),
               SrlGame(3, "Other", "sm64", 0.0, 0))

    givenRaces(race("oot", time(0), result(1, playerName, 123),
                    result(2, "other player", 127)))

    val race = dao.getPlayerByName(playerName)!!.races[0]

    val soft = SoftAssertions()

    race.raceResults.forEach {
      soft.assertThat(it.race.raceResults).contains(it)
    }

    soft.assertAll()
  }

  @Test
  internal fun cachesPlayerInfo() {

    val playerName = UUID.randomUUID().toString()

    givenPlayers(SrlPlayer(0, playerName, "", "", "", "", ""))
    givenGames(SrlGame(1, "OoT", "oot", 0.0, 0))
    givenRaces(race("oot", time(0), result(1, playerName, 123)))

    dao.getPlayerByName(playerName)
    dao.getPlayerByName(playerName.toUpperCase())

    verify(srlHttpClientMock, times(1)).getRacesByPlayerName(playerName)
    verify(srlHttpClientMock, times(1)).getGameByAbbreviation("oot")
    verify(srlHttpClientMock, times(1)).getPlayerByName(playerName)
  }

  @Test
  internal fun clearCacheAtMidnight() {

    val playerName = UUID.randomUUID().toString()

    givenPlayers(SrlPlayer(0, playerName, "", "", "", "", ""))
    givenGames(SrlGame(1, "OoT", "oot", 0.0, 0))
    givenRaces(race("oot", time(0), result(1, playerName, 123)))

    val firstInstant = Instant.ofEpochSecond(Random.nextLong(86401, 123456789))

    `when`(clockMock.instant()).thenReturn(firstInstant)
    dao.getPlayerByName(playerName)

    `when`(clockMock.instant()).thenReturn(firstInstant.plus(1, ChronoUnit.DAYS))
    assertThat(dao.getPlayerByName(playerName.toUpperCase())).isNotNull()

    verify(srlHttpClientMock, times(2)).getRacesByPlayerName(playerName)
    verify(srlHttpClientMock, times(2)).getPlayerByName(anyString())
  }

  private fun givenPlayers(vararg players: SrlPlayer) {

    require(players.map { it.name }.distinct().count() == players.count()) {
      "Two players with the same abbreviation supplied"
    }

    doAnswer {
      players
          .filter { player -> player.name == it.getArgument<String>(0).toLowerCase() }
          .getOrNull(0)
    }.`when`(srlHttpClientMock).getPlayerByName(anyString())
  }

  private fun givenRaces(vararg races: SrlPastRace) {

    doAnswer {
      races
          .asSequence()
          .filter { race ->
            race.results
                .asSequence()
                .any { result -> result.player == it.getArgument(0) }
          }
          .toList()
    }.`when`(srlHttpClientMock).getRacesByPlayerName(anyString())
  }

  private fun givenGames(vararg games: SrlGame) {

    require(games.map { it.abbrev }.distinct().count() == games.count()) {
      "Two games with the same abbreviation supplied"
    }

    games.forEach { allGames[it.abbrev] = it }

    doAnswer {
      games
          .filter { game -> game.abbrev == it.getArgument(0) }
          .getOrNull(0)
    }.`when`(srlHttpClientMock).getGameByAbbreviation(anyString())
  }

  private fun race(game: String, time: ZonedDateTime, vararg results: SrlResult): SrlPastRace {

    require(allGames.containsKey(game)) { "Game not known" }

    return SrlPastRace("0", allGames[game]!!, "", time, results.count().toLong(), results.toList())
  }

  private fun result(place: Long, player: String, time: Long): SrlResult {
    return SrlResult(0, place, player, Duration.of(time, ChronoUnit.SECONDS), "", 0, 0, 0, 0, 0, 0)
  }

  private fun time(timestamp: Long): ZonedDateTime {
    return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC"))
  }
}
