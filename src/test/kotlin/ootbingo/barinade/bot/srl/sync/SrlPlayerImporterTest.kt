package ootbingo.barinade.bot.srl.sync

import com.nhaarman.mockitokotlin2.anyOrNull
import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.model.Player
import ootbingo.barinade.bot.data.model.RaceResult
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

internal class SrlPlayerImporterTest {

  private val srlHttpClientMock = mock(SrlHttpClient::class.java)
  private val srlRaceImporterMock = mock(SrlRaceImporter::class.java)
  private val playerRepositoryMock = mock(PlayerRepository::class.java)

  private val importer = SrlPlayerImporter(srlHttpClientMock, srlRaceImporterMock, playerRepositoryMock)

  @BeforeEach
  internal fun setup() {
    doAnswer {
      it.getArgument(0)
    }.`when`(playerRepositoryMock).save(anyOrNull<Player>())
  }

  @Test
  internal fun returnNullAndNoSaveWhenPlayerUnknown() {

    givenPlayersOnSrl()

    val actualPlayer = importer.importPlayer(UUID.randomUUID().toString())

    assertThat(actualPlayer).isNull()
    verifyZeroInteractions(playerRepositoryMock, srlRaceImporterMock)
  }

  @Test
  internal fun returnsPlayerQueriedAfterRaceImport() {

    val playerId = Random.nextLong(0, 999999)
    val playerName = UUID.randomUUID().toString()

    val raceImported = AtomicBoolean(false)

    doAnswer {
      raceImported.set(true)
    }.`when`(srlRaceImporterMock).importRacesForUser(Player(null, playerId, null, playerName))

    doAnswer {
      val results = if (raceImported.get()) {
        mutableListOf(RaceResult())
      } else {
        mutableListOf()
      }

      Player(null, playerId, null, playerName, null, results)
    }.`when`(playerRepositoryMock).findBySrlNameIgnoreCase(playerName)

    givenPlayersOnSrl(SrlPlayer(playerId, playerName))

    val actualPlayer = importer.importPlayer(playerName)

    assertThat(actualPlayer?.srlId)
        .`as`("Wrong player returned")
        .isEqualTo(playerId)

    assertThat(actualPlayer?.raceResults)
        .`as`("Races not imported")
        .hasSize(1)
  }

  @Test
  internal fun savesPlayerWhenFound() {

    val playerId = Random.nextLong(0, 999999)
    val playerName = UUID.randomUUID().toString()

    givenPlayersOnSrl(SrlPlayer(playerId, playerName))

    importer.importPlayer(playerName)

    verify(playerRepositoryMock).save(Player(null, playerId, null, playerName))
  }

  @Test
  internal fun triggersRaceImportForUser() {

    val playerId = Random.nextLong(0, 999999)
    val playerName = UUID.randomUUID().toString()

    givenPlayersOnSrl(SrlPlayer(playerId, playerName))

    importer.importPlayer(playerName)

    verify(srlRaceImporterMock).importRacesForUser(Player(null, playerId, null, playerName))
  }

  private fun givenPlayersOnSrl(vararg players: SrlPlayer) {

    doAnswer {
      players.findLast { p -> p.name == it.getArgument(0) }
    }.`when`(srlHttpClientMock).getPlayerByName(anyString())
  }
}
