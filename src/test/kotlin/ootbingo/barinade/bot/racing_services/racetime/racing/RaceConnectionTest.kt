package ootbingo.barinade.bot.racing_services.racetime.racing

import com.nhaarman.mockitokotlin2.*
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.Dispatcher
import de.scaramangado.lily.core.communication.MessageInfo
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.IN_PROGRESS
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.*
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.*

internal class RaceConnectionTest {

  //<editor-fold desc="Setup">

  private val connectorMock = mock<WebsocketConnector>()
  private val statusHolder = RaceStatusHolder()
  private val thenDispatcher = mock<Dispatcher>()
  private val websocketMock = mock<RaceWebsocketHandler>()
  private val connection: RaceConnection

  private var disconnectCommandSent = false

  init {
    whenever(connectorMock.connect(any(), any())).thenReturn(websocketMock)
    connection = RaceConnection("", connectorMock, statusHolder, thenDispatcher) { disconnectCommandSent = true }
  }

  @Test
  internal fun opensWebsocket() {

    val url = UUID.randomUUID().toString()

    val connection = RaceConnection(url, connectorMock, RaceStatusHolder(), thenDispatcher) { }

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

    thenNoWelcomeMessageIsSent()
  }

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNotSendWelcomeMessageIfRaceHasAlreadyStarted(status: RacetimeRaceStatus) {

    whenNewRaceUpdateIsReceived(status)

    thenNoWelcomeMessageIsSent()
  }

  //</editor-fold>

  //<editor-fold desc="Race Start">

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
    thenWebsocketIsClosed(false)
  }

  //</editor-fold>

  //<editor-fold desc="Chat Messages">

  @Test
  internal fun dispatchesTextMessages() {

    val message = chatMessage(UUID.randomUUID().toString())

    whenTextMessageReceived(message)

    thenDispatcher wasCalledWithMessage message
  }

  @Test
  internal fun doesNotDispatchBotMessages() {

    val message = chatMessageByBot(UUID.randomUUID().toString())

    whenTextMessageReceived(message)

    thenDispatcher.wasNotCalled()
  }

  @Test
  internal fun doesNotDispatchSystemMessages() {

    val message = chatMessageBySystem(UUID.randomUUID().toString())

    whenTextMessageReceived(message)

    thenDispatcher.wasNotCalled()
  }

  @Test
  internal fun sendsAnswerToChat() {

    val answer = UUID.randomUUID().toString()

    givenDispatcherReturnsAnswerToChatMessage(answer)

    whenTextMessageReceived(chatMessage("anything goes"))

    thenChatMessageMatches(answer)
  }

  //</editor-fold>

  //<editor-fold desc="Disconnect">

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["FINISHED", "CANCELLED"])
  internal fun closesConnectionWhenRaceEnds(newStatus: RacetimeRaceStatus) {

    whenNewRaceUpdateIsReceived(newStatus)

    thenWebsocketIsClosed()
  }

  @Test
  internal fun doesNotCloseConnectionWithoutStatusChange() {
    thenWebsocketIsClosed(false)
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenRaceStatus(status: RacetimeRaceStatus) {
    statusHolder.race = RacetimeRace(name = "oot/abc", status = status)
  }

  private fun givenDispatcherReturnsAnswerToChatMessage(text: String) {
    whenever(thenDispatcher.dispatch(any(), any()))
        .thenReturn(Optional.of(Answer.ofText(text)))
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenNewRaceUpdateIsReceived(newRaceVersion: RacetimeRace) {
    connection.onMessage(RaceUpdate(newRaceVersion))
  }

  private fun whenNewRaceUpdateIsReceived(status: RacetimeRaceStatus) {
    connection.onMessage(RaceUpdate(RacetimeRace(name = "oot/abc", status = status)))
  }

  private fun whenTextMessageReceived(message: ChatMessage) {
    connection.onMessage(message)
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

  private fun thenNoWelcomeMessageIsSent() {
    assertThat(messagesSent).noneMatch { it.startsWith("Welcome") }
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

  private infix fun Dispatcher.wasCalledWithMessage(expectedMessage: ChatMessage) {

    val textCaptor = argumentCaptor<String>()
    val infoCaptor = argumentCaptor<MessageInfo>()

    verify(this).dispatch(textCaptor.capture(), infoCaptor.capture())

    assertThat(textCaptor.lastValue).isEqualTo(expectedMessage.message)
    assertThat((infoCaptor.lastValue as RacetimeMessageInfo).message).isEqualTo(expectedMessage)
  }

  private fun Dispatcher.wasNotCalled() =
      verifyZeroInteractions(this)

  private fun thenWebsocketIsClosed(expected: Boolean = true) {
    assertThat(disconnectCommandSent).isEqualTo(expected)
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

  private fun chatMessage(message: String) =
      ChatMessage(message = message, messagePlain = message, bot = null, isBot = false, isSystem = false)

  private fun chatMessageByBot(message: String) =
      ChatMessage(message = message, messagePlain = message, bot = "BingoBot", isBot = true, isSystem = false)

  private fun chatMessageBySystem(message: String) =
      ChatMessage(message = message, messagePlain = message, bot = null, isBot = false, isSystem = true)

  //</editor-fold>
}
