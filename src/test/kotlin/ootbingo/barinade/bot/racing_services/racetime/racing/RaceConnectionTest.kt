package ootbingo.barinade.bot.racing_services.racetime.racing

import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.Dispatcher
import de.scaramangado.lily.core.communication.MessageInfo
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.*
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.*
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.*
import java.util.*
import kotlin.random.Random

internal class RaceConnectionTest {

  //<editor-fold desc="Setup">

  private val connectorMock = mock<WebsocketConnector>()
  private val statusHolder = RaceStatusHolder()
  private val logicHolder = RaceRoomLogicHolder()
  private val thenDispatcher = mock<Dispatcher>()
  private val websocketMock = mock<RaceWebsocketHandler>()
  private val raceRoomLogicFactoryMock = mock<RaceRoomLogicFactory>()

  private val connection: RaceConnection

  init {
    // Must be in init to set up connectorMock and websocketMock correctly
    whenever(connectorMock.connect(any(), any())).thenReturn(websocketMock)
    connection = RaceConnection("", connectorMock, statusHolder, logicHolder, thenDispatcher, raceRoomLogicFactoryMock) {
      disconnectedWithDelay = it
    }

    logicHolder.logic = mock<BingoRaceRoomLogic>()
    whenever(raceRoomLogicFactoryMock.createLogic(BingoRaceRoomLogic::class, connection)).thenReturn(mock())
  }

  private var disconnectedWithDelay: Boolean? = null

  //</editor-fold>

  //<editor-fold desc="Basic functions">

  @Test
  internal fun opensWebsocket() {

    val url = UUID.randomUUID().toString()

    val connection =
        RaceConnection(url, connectorMock, statusHolder, logicHolder, thenDispatcher, raceRoomLogicFactoryMock) {}

    verify(connectorMock).connect(url, connection)
  }

  @Test
  internal fun persistsRaceStatus() {

    val race = RacetimeRace("oot/${UUID.randomUUID()}")

    whenNewRaceUpdateIsReceived(race)

    thenRaceIsSaved(race)
  }

  //</editor-fold>

  //<editor-fold desc="Initialize Logic">

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"])
  internal fun initializesBingoLogicOnFirstRaceUpdate(status: RacetimeRaceStatus) {

    val race = RacetimeRace(status = status)

    val logic = mock<BingoRaceRoomLogic>()
    givenLogicIsCreated(logic)

    whenNewRaceUpdateIsReceived(race)

    thenLogicIsCreated<BingoRaceRoomLogic>()
    thenLogicIs(logic)
    thenLogicIsInitialized(race)
  }

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNotInitializeIfRaceAlreadyStarted(status: RacetimeRaceStatus) {

    whenNewRaceUpdateIsReceived(RacetimeRace(status = status))

    thenNoLogicIsCreated()
  }

  @ParameterizedTest
  @EnumSource(RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"])
  internal fun doesNotInitializeMultipleTimes(status: RacetimeRaceStatus) {

    givenRaceStatus(status)

    whenNewRaceUpdateIsReceived(RacetimeRace(status = status))

    thenNoLogicIsCreated()
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

  @Test
  internal fun sendsMessageToLogicInsteadOfDispatchingIfSupported() {

    lateinit var receivedCommand: String

    val testCommand = UUID.randomUUID().toString()
    val testMethod: (ChatMessage) -> Unit = { receivedCommand = it.messagePlain }

    val logic = mock<BingoRaceRoomLogic> {
      whenever(it.commands).thenReturn(mapOf(testCommand to testMethod))
    }

    givenLogic(logic)

    val actualMessage = testCommand + UUID.randomUUID().toString()

    whenTextMessageReceived(chatMessage(actualMessage))

    thenDispatcher.wasNotCalled()
    assertThat(receivedCommand).isEqualTo(actualMessage)
  }

  //</editor-fold>

  //<editor-fold desc="Race Changes">

  @Test
  internal fun forwardsRaceChangesToLogic() {

    val race = RacetimeRace("oot/${UUID.randomUUID()}")

    whenNewRaceUpdateIsReceived(race)

    thenRaceIsSentToLogic(race)
  }

  //</editor-fold>

  //<editor-fold desc="RaceRoomDelegate">

  @Test
  internal fun setsGoal() {

    val goal = UUID.randomUUID().toString()

    whenRaceGoalIsSet(goal)

    thenNewRaceGoalMatches(goal)
  }

  @Test
  internal fun sendsMessageText() {

    val message = UUID.randomUUID().toString()
    val pinned = Random.nextBoolean()

    whenMessageIsSent(message, pinned, emptyMap())

    thenSentMessageIsEqualTo(message)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  internal fun sendsMessagePinFlag(pinned: Boolean) {

    whenMessageIsSent(UUID.randomUUID().toString(), pinned, emptyMap())

    thenSentMessageIsPinned(pinned)
  }

  @Test
  internal fun sendsActionButtons() {

    val (name1, name2, action1, action2) = (1..4).map { UUID.randomUUID().toString() }

    val actions = mapOf(
        name1 to RacetimeActionButton(message = action1),
        name2 to RacetimeActionButton(message = action2),
    )

    whenMessageIsSent(UUID.randomUUID().toString(), true, actions)

    thenSentMessageHasActions(actions)
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  internal fun disconnects(withDelay: Boolean) {

    whenDisconnectIsRequested(withDelay)

    thenDisconnectedFromWebsocket(withDelay)
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenLogicIsCreated(logic: RaceRoomLogic) {
    whenever(raceRoomLogicFactoryMock.createLogic(logic::class, connection)).thenReturn(logic)
  }

  private fun givenRaceStatus(status: RacetimeRaceStatus) {
    statusHolder.race = RacetimeRace(name = "oot/abc", status = status)
  }

  private fun givenDispatcherReturnsAnswerToChatMessage(text: String) {
    whenever(thenDispatcher.dispatch(any(), any()))
        .thenReturn(Optional.of(Answer.ofText(text)))
  }

  private fun givenLogic(logic: RaceRoomLogic) {
    logicHolder.logic = logic
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenNewRaceUpdateIsReceived(newRaceVersion: RacetimeRace) {
    connection.onMessage(RaceUpdate(newRaceVersion))
  }

  private fun whenTextMessageReceived(message: ChatMessage) {
    connection.onMessage(message)
  }

  private fun whenRaceGoalIsSet(goal: String) {
    connection.setGoal(goal)
  }

  private fun whenMessageIsSent(message: String, pinned: Boolean, actions: Map<String, RacetimeActionButton>?) {
    connection.sendMessage(message, pinned, actions)
  }

  private fun whenDisconnectIsRequested(withDelay: Boolean) {
    connection.closeConnection(withDelay)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenRaceIsSaved(race: RacetimeRace) {
    assertThat(statusHolder.race).isEqualTo(race)
  }

  private fun thenRaceIsSentToLogic(expectedRace: RacetimeRace) {
    verify(logicHolder.logic).onRaceUpdate(expectedRace)
  }

  private fun thenSentMessageIsEqualTo(expectedMessage: String) {
    verify(websocketMock).sendMessage(eq(expectedMessage), any(), anyOrNull())
  }

  private fun thenSentMessageIsPinned(expectedPinned: Boolean) {
    verify(websocketMock).sendMessage(any(), eq(expectedPinned), anyOrNull())
  }

  private fun thenSentMessageHasActions(expectedActions: Map<String, RacetimeActionButton>) {
    verify(websocketMock).sendMessage(any(), any(), eq(expectedActions))
  }

  private fun thenChatMessageMatches(regex: String) {
    assertThat(messagesSent).anyMatch { it.matches(Regex(regex)) }
  }

  private fun thenNewRaceGoalMatches(regex: String) {
    assertThat(goal).matches(regex)
  }

  private infix fun Dispatcher.wasCalledWithMessage(expectedMessage: ChatMessage) {

    val textCaptor = argumentCaptor<String>()
    val infoCaptor = argumentCaptor<MessageInfo>()

    verify(this).dispatch(textCaptor.capture(), infoCaptor.capture())

    assertThat(textCaptor.lastValue).isEqualTo(expectedMessage.message)
    assertThat((infoCaptor.lastValue as RacetimeMessageInfo).message).isEqualTo(expectedMessage)
  }

  private fun Dispatcher.wasNotCalled() =
      verifyNoInteractions(this)

  private inline fun <reified T : RaceRoomLogic> thenLogicIsCreated() {
    verify(raceRoomLogicFactoryMock).createLogic(T::class, connection)
  }

  private fun thenNoLogicIsCreated() {
    verifyNoInteractions(raceRoomLogicFactoryMock)
  }

  private fun thenLogicIs(expectedLogic: RaceRoomLogic) {
    assertThat(logicHolder.logic).isEqualTo(expectedLogic)
  }

  private fun thenLogicIsInitialized(expectedRace: RacetimeRace) {
    verify(logicHolder.logic).initialize(expectedRace)
  }

  private fun thenDisconnectedFromWebsocket(withDelay: Boolean) {
    assertThat(disconnectedWithDelay).isEqualTo(withDelay)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private val messagesSent: List<String>
    get() =
      argumentCaptor<String>()
          .also { verify(websocketMock, atLeast(0)).sendMessage(it.capture(), any(), anyOrNull()) }
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
