package ootbingo.barinade.bot.racing_services.racetime.racing

import com.nhaarman.mockitokotlin2.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.IN_PROGRESS
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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

  @Test
  internal fun persistsRaceStatus() {

    val race = RacetimeRace("oot/${UUID.randomUUID()}")

    whenNewRaceUpdateIsReceived(race)

    thenRaceIsSaved(race)
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

    givenRaceStatus(RacetimeRaceStatus.OPEN)

    whenNewRaceUpdateIsReceived(RacetimeRace("oot/abc"))

    thenNoChatMessageIsSent()
  }

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNotSendWelcomeMessageIfRaceHasAlreadyStarted(status: RacetimeRaceStatus) {

    whenNewRaceUpdateIsReceived(status)

    thenNoChatMessageIsSent()
  }

  //</editor-fold>

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL", "PENDING"])
  internal fun setsGoalWhenRaceStarts(status: RacetimeRaceStatus) {

    givenRaceStatus(status)

    whenNewRaceUpdateIsReceived(IN_PROGRESS)

    thenNewRaceGoalMatches("https://ootbingo.github.io/bingo/.*/bingo.html?.*")
  }

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL", "PENDING"], mode = EnumSource.Mode.EXCLUDE)
  internal fun onlySetGoalOnce(status: RacetimeRaceStatus) {

    givenRaceStatus(status)

    whenNewRaceUpdateIsReceived(IN_PROGRESS)

    thenGoalIsNotChanged()
  }

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL", "PENDING"])
  internal fun postsGoalAndFilenameWhenRaceStarts(status: RacetimeRaceStatus) {

    givenRaceStatus(status)

    whenNewRaceUpdateIsReceived(IN_PROGRESS)

    thenChatMessageMatches("Filename: [A-Z]{2}")
    thenChatMessageMatches("Goal: https://ootbingo.github.io/bingo/.*/bingo.html?.*")
  }

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL", "PENDING"], mode = EnumSource.Mode.EXCLUDE)
  internal fun onlyPostsGoalAndFilenameOnce(status: RacetimeRaceStatus) {

    givenRaceStatus(status)

    whenNewRaceUpdateIsReceived(IN_PROGRESS)

    thenNoChatMessageIsSent()
  }

  //<editor-fold desc="Given">

  private fun givenRaceStatus(status: RacetimeRaceStatus) {
    statusHolder.race = RacetimeRace(name = "oot/abc", status = status)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenNewRaceUpdateIsReceived(newRaceVersion: RacetimeRace) {
    connection.onMessage(RaceUpdate(newRaceVersion))
  }

  private fun whenNewRaceUpdateIsReceived(status: RacetimeRaceStatus) {
    connection.onMessage(RaceUpdate(RacetimeRace(name = "oot/abc", status = status)))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenRaceIsSaved(race: RacetimeRace) {
    assertThat(statusHolder.race).isEqualTo(race)
  }

  private fun thenWelcomeMessageIsSent() {
    assertThat(messagesSent).anyMatch { it.startsWith("Welcome") }
  }

  private fun thenNoChatMessageIsSent() {
    assertThat(messagesSent).isEmpty()
  }

  private fun thenChatMessageMatches(regex: String) {
    assertThat(messagesSent).anyMatch { it.matches(Regex(regex)) }
  }

  private fun thenNewRaceGoalMatches(regex: String) {
    assertThat(goal).matches(regex)
  }

  private fun thenGoalIsNotChanged() {
    verify(websocketMock, never()).setGoal(any())
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private val messagesSent: List<String>
    get() =
      argumentCaptor<String>()
          .also { verify(websocketMock, atLeast(0)).sendMessage(it.capture()) }
          .allValues

  private val goal: String
    get() =
      argumentCaptor<String>()
          .also { verify(websocketMock).setGoal(it.capture()) }
          .lastValue

  //</editor-fold>
}
