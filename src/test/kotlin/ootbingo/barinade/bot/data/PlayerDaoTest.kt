package ootbingo.barinade.bot.data

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

  private val knownPlayers = mutableMapOf<String, Player>()

  @BeforeEach
  internal fun setup() {
    doAnswer {
      knownPlayers[it.getArgument(0)]
    }.`when`(playerRepositoryMock).findBySrlNameIgnoreCase(anyString())
  }

  @Test
  internal fun returnsNullIfPlayerNotKnown() {

    givenPlayersOnSrl()
    givenPlayersInDb()

    assertThat(dao.getPlayerByName("some name")).isNull()
  }

  @Test
  internal fun readsPlayerFromDbFirst() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersOnSrl(srlPlayer(playerId, playerName))
    givenPlayersInDb(player(playerId, playerName))

    val actualPlayer = dao.getPlayerByName(playerName)

    assertThat(actualPlayer!!.srlName).isEqualTo(playerName)
    assertThat(actualPlayer.srlId).isEqualTo(playerId)
    verifyZeroInteractions(srlPlayerImporterMock)
  }

  private fun givenPlayersInDb(vararg players: Player) {
    players.forEach { knownPlayers[it.srlName] = it }
  }

  private fun givenPlayersOnSrl(vararg srlPlayers: SrlPlayer) {

    doAnswer {
      val player = srlPlayers.findLast { p -> p.name == it.getArgument(0) }
      if (player != null) {
        knownPlayers[player.name] = Player(player.id, player.name)
      }
      player?.let { Player(it, listOf()) }
    }.`when`(srlPlayerImporterMock).importPlayer(anyString())
  }

  private fun player(id: Long, username: String): Player = Player(id, username)

  private fun srlPlayer(id: Long, username: String): SrlPlayer = SrlPlayer(id, username)

  @Test
  internal fun triggersUserImportIfUserNotInDb() {

    val playerName = UUID.randomUUID().toString()
    val playerId = Random.nextLong(0, 10000)

    givenPlayersInDb()
    givenPlayersOnSrl(srlPlayer(playerId, playerName))

    val actualPlayer = dao.getPlayerByName(playerName)

    assertThat(actualPlayer).isEqualTo(Player(playerId, playerName))
    verify(srlPlayerImporterMock).importPlayer(playerName)
  }

  @Test
  internal fun redirectsResultQuery() {

    val playerName = UUID.randomUUID().toString()
    val playerRepositoryMock = mock(PlayerRepository::class.java)
    val expectedResult = listOf<ResultInfo>()

    `when`(playerRepositoryMock.findResultsForPlayer(playerName)).thenReturn(expectedResult)

    assertThat(dao.findResultsForPlayer(playerName)).isEqualTo(expectedResult)
  }
}
