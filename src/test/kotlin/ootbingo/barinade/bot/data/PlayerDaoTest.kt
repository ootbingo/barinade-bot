package ootbingo.barinade.bot.data

import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.data.model.helper.ResultInfo
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.Race
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import ootbingo.barinade.bot.srl.api.model.SrlGame
import ootbingo.barinade.bot.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import ootbingo.barinade.bot.srl.api.model.SrlResult
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random

internal class PlayerDaoTest {

  private val srlHttpClientMock = mock(SrlHttpClient::class.java)

  private val savedPlayers = HashMap<String, Player>()
  private val savedGoals = mutableListOf<String>()

  private val playerRepositoryMock = object : PlayerRepository {
    override fun save(player: Player): Player {
      savedPlayers[player.srlName.toLowerCase()] = player
      return player
    }

    override fun findBySrlNameIgnoreCase(srlName: String): Player? =
        savedPlayers[srlName.toLowerCase()]

    override fun findResultsForPlayer(username: String): List<ResultInfo> {
      throw NotImplementedError("Not implemented")
    }
  }

  private val raceRepository = object : RaceRepository {
    override fun save(race: Race): Race {

      if (!savedGoals.contains(race.goal)) {
        savedGoals.add(race.goal)
      }

      race.raceResults.forEach {
        val player = it.player
        if (player.raceResults.none { result -> result.id == race.srlId.toLong() }) {
          player.raceResults.add(it.copy(id = race.srlId.toLong()))
        }
      }

      return race
    }

    override fun findBySrlId(srlId: String): Race? {
      return null
    }
  }

  private val allGames = HashMap<String, SrlGame>()

  private val dao = PlayerDao(srlHttpClientMock, playerRepositoryMock, raceRepository)

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
  internal fun savesGoalsOfRaces() {

    val playerName = UUID.randomUUID().toString()
    val goal = UUID.randomUUID().toString()

    givenPlayers(SrlPlayer(0, playerName, "", "", "", "", ""))
    givenGames(SrlGame(1, "OoT", "oot", 0.0, 0))
    givenRaces(race("oot", time(0), goal, result(1, playerName, 123)))

    dao.getPlayerByName(playerName)!!.races[0]

    assertThat(savedGoals).containsExactly(goal)
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
  internal fun readsPlayerFromDb() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayers()
    playerRepositoryMock.save(Player(playerId, playerName, mutableListOf()))

    val actualPlayer = dao.getPlayerByName(playerName)

    assertThat(actualPlayer!!.srlName).isEqualTo(playerName)
    assertThat(actualPlayer.srlId).isEqualTo(playerId)
  }

  @Test
  internal fun redirectsResultQuery() {

    val playerName = UUID.randomUUID().toString()
    val playerRepositoryMock = mock(PlayerRepository::class.java)
    val expectedResult = listOf<ResultInfo>()

    `when`(playerRepositoryMock.findResultsForPlayer(playerName)).thenReturn(expectedResult)

    val dao = PlayerDao(mock(SrlHttpClient::class.java), playerRepositoryMock, mock(RaceRepository::class.java))

    assertThat(dao.findResultsForPlayer(playerName)).isEqualTo(expectedResult)
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

  private fun race(game: String, time: ZonedDateTime, goal: String, vararg results: SrlResult): SrlPastRace {

    require(allGames.containsKey(game)) { "Game not known" }

    return SrlPastRace("${Random.nextLong()}", allGames[game]!!, goal, time, results.count().toLong(), results.toList())
  }

  private fun race(game: String, time: ZonedDateTime, vararg results: SrlResult): SrlPastRace =
      race(game, time, "", *results)

  private fun result(place: Long, player: String, time: Long): SrlResult {
    return SrlResult(Random.nextLong(), place, player, Duration.of(time, ChronoUnit.SECONDS), "", 0, 0, 0, 0, 0, 0)
  }

  private fun time(timestamp: Long): ZonedDateTime {
    return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC"))
  }
}
