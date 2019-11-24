package ootbingo.barinade.bot.srl.sync

import com.nhaarman.mockitokotlin2.doAnswer
import ootbingo.barinade.bot.data.connection.PlayerRepository
import ootbingo.barinade.bot.data.connection.RaceRepository
import ootbingo.barinade.bot.srl.api.client.SrlHttpClient
import ootbingo.barinade.bot.srl.api.model.SrlGame
import ootbingo.barinade.bot.srl.api.model.SrlPastRace
import ootbingo.barinade.bot.srl.api.model.SrlPlayer
import ootbingo.barinade.bot.srl.api.model.SrlResult
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.UUID

@DataJpaTest
internal class ImportIntegrationTest(@Autowired private val playerRepository: PlayerRepository,
                                     @Autowired private val raceRepository: RaceRepository) {

  private val srlHttpClientMock = mock(SrlHttpClient::class.java)
  private val raceImporter = SrlRaceImporter(srlHttpClientMock, raceRepository)
  private val srlPlayerImporter = SrlPlayerImporter(srlHttpClientMock, raceImporter, playerRepository, raceRepository)

  private val oot = SrlGame(1998, "Ocarina of Time", "oot", 1.0, 1)

  @BeforeEach
  internal fun setup() {
    `when`(srlHttpClientMock.getGameByAbbreviation("oot")).thenReturn(oot)
  }

  @Test
  @DirtiesContext
  internal fun addsTwoPlayersWithCommonRaceSeparately() {

    val username1 = UUID.randomUUID().toString()
    val username2 = UUID.randomUUID().toString()

    givenPlayersOnSrl(srlPlayer(37, username1), srlPlayer(42, username2))

    givenRacesOnSrl(srlBingoRace("1", srlResult(1, username1, 321), srlResult(2, "other", 322)),
                    srlBingoRace("5", srlResult(1, username2, 123), srlResult(2, "other", 124)),
                    srlBingoRace("9", srlResult(1, username1, 100), srlResult(2, username2, 101)))

    srlPlayerImporter.importPlayer(username1)
    assertThat(raceRepository.findBySrlId("9")?.raceResults).hasSize(1)

    srlPlayerImporter.importPlayer(username2)

    assertThat(raceRepository.findBySrlId("1")?.raceResults).hasSize(1)
    assertThat(raceRepository.findBySrlId("5")?.raceResults).hasSize(1)
    assertThat(raceRepository.findBySrlId("9")?.raceResults?.map { it.player.srlName })
        .containsExactlyInAnyOrder(username1, username2)
  }

  //<editor-fold desc="Given">

  private fun givenRacesOnSrl(vararg srlRaces: SrlPastRace) {

    doAnswer {
      srlRaces.filter { r ->
        r.results
            .map { res -> res.player }
            .contains(it.getArgument(0))
      }.toList()
    }.`when`(srlHttpClientMock).getRacesByPlayerName(anyString())
  }

  private fun givenPlayersOnSrl(vararg srlPlayers: SrlPlayer) {

    require(srlPlayers.map { it.name }.groupingBy { it }.eachCount().map { it.value }.none { it > 1 }) {
      "Usernames must be distinct"
    }

    require(srlPlayers.map { it.id }.groupingBy { it }.eachCount().map { it.value }.none { it > 1 }) {
      "IDs must be distinct"
    }

    doAnswer {
      srlPlayers.lastOrNull { p -> p.name == it.getArgument(0) }
    }.`when`(srlHttpClientMock).getPlayerByName(anyString())
  }

  //</editor-fold>

  //<editor-fold desc="Data Factories">

  private fun srlPlayer(id: Long, username: String) = SrlPlayer(id, username)

  private fun srlBingoRace(id: String, vararg srlResults: SrlResult) =
      SrlPastRace(id, oot, numentrants = srlResults.size.toLong(), results = srlResults.toList())

  private fun srlResult(place: Long, player: String, time: Long) =
      SrlResult(0, place, player, Duration.ofSeconds(time))

  //</editor-fold>
}
