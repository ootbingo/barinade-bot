package ootbingo.barinade.bot.racing_services.racetime.racing

import com.nhaarman.mockitokotlin2.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class RaceConnectionTest {

  //<editor-fold desc="Setup">

  private val connectorMock = mock<WebsocketConnector>()
  private val statusHolder = RaceStatusHolder()
  private val websocketMock = mock<RaceWebsocketHandler>()
  private val connection: RaceConnection

  init {
    whenever(connectorMock.connect(any(), any())).thenReturn(websocketMock)
    connection = RaceConnection("", connectorMock, statusHolder)
  }

  @Test
  internal fun opensWebsocket() {

    val url = UUID.randomUUID().toString()

    val connection = RaceConnection(url, connectorMock, RaceStatusHolder())

    verify(connectorMock).connect(url, connection)
  }

  //</editor-fold>

  //<editor-fold desc="Welcome Message">

  @Test
  internal fun sendsWelcomeMessage() {

    whenNewRaceUpdateIsReceived(RacetimeRace("oot/abc"))

    thenWelcomeMessageIsSent()
  }

  @Test
  internal fun onlySendsWelcomeOnce() {

    givenRaceStatus(RacetimeRace.RacetimeRaceStatus.OPEN)

    whenNewRaceUpdateIsReceived(RacetimeRace("oot/abc"))

    thenNoChatMessageIsSent()
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenRaceStatus(status: RacetimeRace.RacetimeRaceStatus) {
    statusHolder.race = RacetimeRace(name = "oot/abc", status = status)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenNewRaceUpdateIsReceived(newRaceVersion: RacetimeRace) {
    connection.onMessage(RaceUpdate(newRaceVersion))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenWelcomeMessageIsSent() {
    assertThat(messagesSent).anyMatch { it.startsWith("Welcome") }
  }

  private fun thenNoChatMessageIsSent() {
    assertThat(messagesSent).isEmpty()
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private val messagesSent: List<String>
    get() =
      argumentCaptor<String>()
          .also { verify(websocketMock, atLeast(0)).sendMessage(it.capture()) }
          .allValues

  //</editor-fold>
}
