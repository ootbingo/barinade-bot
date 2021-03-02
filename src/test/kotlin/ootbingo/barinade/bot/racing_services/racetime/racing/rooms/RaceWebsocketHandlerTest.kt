package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClientConfiguration
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*

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

  private fun whenChatMessageIsSent(message: String) =
    handler.sendMessage(message)

  private fun whenGoalIsSet(goal: String) =
      handler.setGoal(goal)

  private lateinit var thenAction: RacetimeAction

  private infix fun RacetimeAction.isChatMessage(message: String) {
    assertThat(action).isEqualTo("message")
    assertThat((data as SendMessage).message).isEqualTo(message)
  }

  private infix fun RacetimeAction.isNewGoal(goal: String) {
    assertThat(action).isEqualTo("setinfo")
    assertThat((data as SetGoal).info).isEqualTo(goal)
  }
}
