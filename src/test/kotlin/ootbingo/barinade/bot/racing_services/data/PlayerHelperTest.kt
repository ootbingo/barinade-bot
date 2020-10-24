package ootbingo.barinade.bot.racing_services.data

import com.nhaarman.mockitokotlin2.whenever
import ootbingo.barinade.bot.racing_services.data.connection.PlayerRepository
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.UUID
import kotlin.random.Random

internal class PlayerHelperTest {

  private val playerRepositoryMock = mock(PlayerRepository::class.java)

  private val dao = PlayerHelper(playerRepositoryMock)

  private val dbPlayers = mutableListOf<Player>()

  @BeforeEach
  internal fun setup() {

    doAnswer {
      dbPlayers.lastOrNull { p -> it.getArgument<String>(0).equals(p.srlName, true) }
    }.whenever(playerRepositoryMock).findBySrlNameIgnoreCase(anyString())

    doAnswer {
      dbPlayers.lastOrNull { p -> it.getArgument<String>(0).equals(p.racetimeName, true) }
    }.whenever(playerRepositoryMock).findByRacetimeNameIgnoreCase(anyString())
  }

  @Test
  internal fun returnsNullIfPlayerNotKnown() {

    givenPlayersInDb()

    assertThat(dao.getPlayerByName("some name")).isNull()
  }

  @Test
  internal fun readsPlayerFromDbByRacetimeNameFirst() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersInDb(Player(playerId, 0, "", "srlName", playerName),
                     Player(-1, 0, "", playerName, "racetimeName"))

    val actualPlayer = dao.getPlayerByName(playerName)!!

    assertThat(actualPlayer.id).isEqualTo(playerId)
  }

  @Test
  internal fun readsPlayerFromDbBySrlNameSecond() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersInDb(Player(playerId, 0, "", playerName, "racetimeName"))

    val actualPlayer = dao.getPlayerByName(playerName)!!

    assertThat(actualPlayer.id).isEqualTo(playerId)
  }

  @Test
  internal fun redirectsResultQuery() {

    val player = Player(42)
    val playerRepositoryMock = mock(PlayerRepository::class.java)
    val expectedResult = listOf<ResultInfo>()

    whenever(playerRepositoryMock.findResultsForPlayer(player)).thenReturn(expectedResult)

    assertThat(dao.findResultsForPlayer(player)).isEqualTo(expectedResult)
  }

  private fun givenPlayersInDb(vararg players: Player) {

    dbPlayers.addAll(players)
  }
}
