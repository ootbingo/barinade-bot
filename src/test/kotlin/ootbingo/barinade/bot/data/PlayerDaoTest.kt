package ootbingo.barinade.bot.data

import com.nhaarman.mockitokotlin2.whenever
import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.helper.ResultInfo
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import ootbingo.barinade.bot.srl.sync.SrlPlayerImporter
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.UUID
import kotlin.random.Random

internal class PlayerDaoTest {

  private val srlPlayerImporterMock = mock(SrlPlayerImporter::class.java)
  private val playerRepositoryMock = mock(PlayerRepository::class.java)

  private val dao = PlayerDao(playerRepositoryMock, srlPlayerImporterMock)

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

    givenPlayersOnSrl()
    givenPlayersInDb()

    assertThat(dao.getPlayerByName("some name")).isNull()
  }

  @Test
  internal fun readsPlayerFromDbByRacetimeNameFirst() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersOnSrl(srlPlayer(playerId, playerName))
    givenPlayersInDb(Player(playerId, 0, "", "srlName", playerName),
                     Player(-1, 0, "", playerName, "racetimeName"))

    val actualPlayer = dao.getPlayerByName(playerName)!!

    assertThat(actualPlayer.id).isEqualTo(playerId)
    verifyZeroInteractions(srlPlayerImporterMock)
  }

  @Test
  internal fun readsPlayerFromDbBySrlNameSecond() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersOnSrl(srlPlayer(playerId, playerName))
    givenPlayersInDb(Player(playerId, 0, "", playerName, "racetimeName"))

    val actualPlayer = dao.getPlayerByName(playerName)!!

    assertThat(actualPlayer.id).isEqualTo(playerId)
    verifyZeroInteractions(srlPlayerImporterMock)
  }

  @Test
  internal fun triggersUserImportIfUserNotInDb() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersInDb()
    givenPlayersOnSrl(srlPlayer(playerId, playerName))

    val actualPlayer = dao.getPlayerByName(playerName)

    assertThat(actualPlayer).isEqualTo(Player(null, playerId, null, playerName))
    verify(srlPlayerImporterMock).importPlayer(playerName)
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

  private fun givenPlayersOnSrl(vararg srlPlayers: SrlPlayer) {

    doAnswer {
          srlPlayers
              .findLast { p -> p.name == it.getArgument(0) }
              ?.let { Player(it, listOf()) }
              ?.also  { dbPlayers.add(it) }
    }.whenever(srlPlayerImporterMock).importPlayer(anyString())
  }

  private fun srlPlayer(id: Long, username: String): SrlPlayer = SrlPlayer(id, username)
}
