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

  private val knownSrlPlayers = mutableMapOf<String, Player>()

  @BeforeEach
  internal fun setup() {
    doAnswer {
      knownSrlPlayers[it.getArgument(0)]
    }.`when`(playerRepositoryMock).findBySrlNameIgnoreCase(anyString())
  }

  @Test
  internal fun returnsNullIfPlayerNotKnown() {

    givenPlayersOnSrl()
    givenSrlPlayersInDb()

    assertThat(dao.getPlayerByName("some name")).isNull()
  }

  @Test
  internal fun readsPlayerFromDbFirst() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersOnSrl(srlPlayer(playerId, playerName))
    givenSrlPlayersInDb(player(playerId, playerName))

    val actualPlayer = dao.getPlayerByName(playerName)

    assertThat(actualPlayer!!.srlName).isEqualTo(playerName)
    assertThat(actualPlayer.srlId).isEqualTo(playerId)
    verifyZeroInteractions(srlPlayerImporterMock)
  }

  private fun givenSrlPlayersInDb(vararg players: Player) {
    players
        .filterNot { it.srlName == null }
        .forEach { knownSrlPlayers[it.srlName!!] = it }
  }

  private fun givenPlayersOnSrl(vararg srlPlayers: SrlPlayer) {

    doAnswer {
      val player = srlPlayers.findLast { p -> p.name == it.getArgument(0) }
      if (player != null) {
        knownSrlPlayers[player.name] = Player(null, player.id, null, player.name)
      }
      player?.let { Player(it, listOf()) }
    }.`when`(srlPlayerImporterMock).importPlayer(anyString())
  }

  private fun player(id: Long, username: String): Player = Player(null, id, null, username)

  private fun srlPlayer(id: Long, username: String): SrlPlayer = SrlPlayer(id, username)

  @Test
  internal fun triggersUserImportIfUserNotInDb() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenSrlPlayersInDb()
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
}
