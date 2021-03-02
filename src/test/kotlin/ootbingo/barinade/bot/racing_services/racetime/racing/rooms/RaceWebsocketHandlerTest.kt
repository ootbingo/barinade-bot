package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import com.nhaarman.mockitokotlin2.*
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClientConfiguration
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*
import kotlin.random.Random

internal class RaceWebsocketHandlerTest {

  //<editor-fold desc="Setup">

  private val raceConnectionMock = mock<RaceConnection>()
  private val sessionMock = mock<WebSocketSession>()
  private val gson = RacetimeHttpClientConfiguration().racetimeGson()

  private val handler = RaceWebsocketHandler(raceConnectionMock, gson)

  @BeforeEach
  internal fun setup() {

    handler.afterConnectionEstablished(sessionMock)

    doAnswer {
      thenAction = gson.fromJson(it.getArgument<TextMessage>(0).payload, RacetimeAction::class.java)
    }.whenever(sessionMock).sendMessage(any())
  }

  //</editor-fold>

  //<editor-fold desc="Tests: Send">

  @Test
  internal fun sendsChatMessage() {

    val message = UUID.randomUUID().toString()

    whenChatMessageIsSent(message)

    thenAction isChatMessage message
  }

  @Test
  internal fun setsGoal() {

    val goal = UUID.randomUUID().toString()

    whenGoalIsSet(goal)

    thenAction isNewGoal goal
  }

  //</editor-fold>

  //<editor-fold desc="Tests: Receive">

  @Test
  internal fun forwardsChatMessage() {

    val message = UUID.randomUUID().toString()

    whenMessageIsReceived(chatMessage(message))

    thenDelegateReceivesChatMessage(message)
  }

  @Test
  internal fun forwardsRaceUpdate() {

    val version = Random.nextInt()

    whenMessageIsReceived(raceUpdate(version))

    thenDelegateReceivesRaceInfo(version)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenChatMessageIsSent(message: String) =
      handler.sendMessage(message)

  private fun whenGoalIsSet(goal: String) =
      handler.setGoal(goal)

  private fun whenMessageIsReceived(message: String) {
    handler.handleMessage(sessionMock, TextMessage(message))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private lateinit var thenAction: RacetimeAction

  private infix fun RacetimeAction.isChatMessage(message: String) {
    assertThat(action).isEqualTo("message")
    assertThat((data as SendMessage).message).isEqualTo(message)
  }

  private infix fun RacetimeAction.isNewGoal(goal: String) {
    assertThat(action).isEqualTo("setinfo")
    assertThat((data as SetGoal).info).isEqualTo(goal)
  }

  private fun thenDelegateReceivesChatMessage(message: String) {

    val captor = argumentCaptor<RacetimeMessage>()
    verify(raceConnectionMock).onMessage(captor.capture())

    assertThat((captor.lastValue as ChatMessage).message).isEqualTo(message)
  }

  private fun thenDelegateReceivesRaceInfo(version: Int) {

    val captor = argumentCaptor<RacetimeMessage>()
    verify(raceConnectionMock).onMessage(captor.capture())

    assertThat((captor.lastValue as RaceUpdate).race.version).isEqualTo(version)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private fun chatMessage(message: String) =
      gson.toJson(mapOf("type" to "chat.message", "message" to ChatMessage(message = message)))

  private fun raceUpdate(version: Int) =
      gson.toJson(mapOf("type" to "race.data", "race" to RaceUpdate(RacetimeRace(version = version))))

  //</editor-fold>
}
