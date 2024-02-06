package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import java.util.*
import kotlin.reflect.KClass

class BingoRaceRoomLogicTest {

  //<editor-fold desc="Setup">

  private val statusHolder = RaceStatusHolder()
  private val delegateMock = mock<RaceRoomDelegate>()

  private val logic = BingoRaceRoomLogic(statusHolder, delegateMock)

  @BeforeEach
  internal fun setup() {
    givenRaceStatus(OPEN)
  }

  //</editor-fold>

  //<editor-fold desc="Test: General">

  @Test
  internal fun persistsRaceStatus() {

    val race = RacetimeRace("oot/${UUID.randomUUID()}")

    whenNewRaceUpdateIsReceived(race)

    thenRaceIsSaved(race)
  }

  //</editor-fold>

  //<editor-fold desc="Test: Welcome Message">

  @Test
  internal fun sendsWelcomeMessage() {

    whenLogicIsInitialized(RacetimeRace("oot/abc"))

    thenWelcomeMessageIsSent()
  }

  @ParameterizedTest
  @EnumSource(RacetimeRace.RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNotSendWelcomeMessageIfRaceHasAlreadyStarted(status: RacetimeRace.RacetimeRaceStatus) {

    whenLogicIsInitialized(RacetimeRace("oot/abc", status = status))

    thenNoWelcomeMessageIsSent()
  }

  @ParameterizedTest
  @EnumSource(RacetimeRace.RacetimeRaceStatus::class)
  internal fun persistsInitialRaceStatus(status: RacetimeRace.RacetimeRaceStatus) {

    val race = RacetimeRace("oot/abc", status = status)

    whenLogicIsInitialized(race)

    thenRaceIsSaved(race)
  }

  //</editor-fold>

  //<editor-fold desc="Test: Disconnect">

  @ParameterizedTest
  @EnumSource(RacetimeRace.RacetimeRaceStatus::class, names = ["FINISHED", "CANCELLED"])
  internal fun closesConnectionWhenRaceEnds(newStatus: RacetimeRace.RacetimeRaceStatus) {

    whenNewRaceUpdateIsReceived(newStatus)

    thenWebsocketIsClosed(delay = true)
  }

  @Test
  internal fun doesNotCloseConnectionWithoutStatusChange() {
    thenWebsocketIsClosed(false)
  }

  //</editor-fold>

  //<editor-fold desc="Test: Race Modes">

  @Test
  internal fun blackoutMode() {

    givenRaceStatus(OPEN)

    whenTextMessageReceived("!short")
    whenTextMessageReceived("!blackout")

    whenNewRaceUpdateIsReceived(RacetimeRace.RacetimeRaceStatus.IN_PROGRESS)

    thenChatMessageMatches("Goal: .*bingo.html?.*&mode=blackout(&.*|$)")
  }

  @Test
  internal fun shortMode() {

    givenRaceStatus(OPEN)

    whenTextMessageReceived("!blackout")
    whenTextMessageReceived("!short")

    whenNewRaceUpdateIsReceived(RacetimeRace.RacetimeRaceStatus.IN_PROGRESS)

    thenChatMessageMatches("Goal: .*bingo.html?.*&mode=short(&.*|$)")
  }

  @Test
  internal fun normalMode() {

    givenRaceStatus(OPEN)

    whenTextMessageReceived("!nobingo")
    whenTextMessageReceived("!normal")

    whenNewRaceUpdateIsReceived(RacetimeRace.RacetimeRaceStatus.IN_PROGRESS)

    thenChatMessageMatches("Goal: .*bingo.html?.*&mode=normal(&.*|$)")
  }

  @Test
  internal fun noBingo() {

    givenRaceStatus(OPEN)

    whenTextMessageReceived("!normal")
    whenTextMessageReceived("!nobingo")

    whenNewRaceUpdateIsReceived(RacetimeRace.RacetimeRaceStatus.IN_PROGRESS)

    thenGoalIsNotChanged()
  }

  @Test
  internal fun childMode() {

    givenRaceStatus(OPEN)

    whenTextMessageReceived("!child")

    whenNewRaceUpdateIsReceived(RacetimeRace.RacetimeRaceStatus.IN_PROGRESS)

    thenChatMessageMatches("Goal: https://doctorno124.github.io/childkek/bingo.html?.*&mode=normal(&.*|$)")
  }

  //</editor-fold>

  //<editor-fold desc="Test: Team Races">

  @Test
  internal fun initialModeNormal() {

    whenLogicIsInitialized(RacetimeRace(name = "oot/abc", status = OPEN, teamRace = false))
    thenChatMessageMatches("Current mode: normal")

    whenNewRaceUpdateIsReceived(IN_PROGRESS)

    thenChatMessageMatches("Goal: .*bingo.html?.*&mode=normal(&.*|$)")
  }

  @Test
  internal fun initialModeBlackoutForTeamRaces() {

    whenLogicIsInitialized(RacetimeRace(name = "oot/abc", status = OPEN, teamRace = true))
    thenChatMessageMatches("Current mode: blackout")

    whenNewRaceUpdateIsReceived(IN_PROGRESS)

    thenChatMessageMatches("Goal: .*bingo.html?.*&mode=blackout(&.*|$)")
  }

  //</editor-fold>

  //<editor-fold desc="Test: Initialize Anti-Bingo">

  @ParameterizedTest
  @EnumSource(RacetimeRace.RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"])
  internal fun requestsLogicChangeWhenAntiBingoIsStarted(status: RacetimeRace.RacetimeRaceStatus) {

    givenRaceStatus(status)

    whenTextMessageReceived("!anti")

    thenLogicChangeIsRequested(AntiBingoRaceRoomLogic::class)
  }

  @ParameterizedTest
  @EnumSource(RacetimeRace.RacetimeRaceStatus::class, names = ["OPEN", "INVITATIONAL"], mode = EnumSource.Mode.EXCLUDE)
  internal fun doesNotRequestsAntiBingoAfterRaceHasStarted(status: RacetimeRace.RacetimeRaceStatus) {

    givenRaceStatus(status)

    whenTextMessageReceived("!anti")

    thenNoLogicChangeIsRequested()
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenRaceStatus(status: RacetimeRace.RacetimeRaceStatus) {
    statusHolder.race = RacetimeRace(name = "oot/abc", status = status)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenLogicIsInitialized(initialRace: RacetimeRace) {
    logic.initialize(initialRace)
  }

  private fun whenNewRaceUpdateIsReceived(newRaceVersion: RacetimeRace) {
    logic.onRaceUpdate(newRaceVersion)
  }

  private fun whenNewRaceUpdateIsReceived(status: RacetimeRace.RacetimeRaceStatus) {
    logic.onRaceUpdate(RacetimeRace(name = "oot/abc", status = status))
  }

  private fun whenTextMessageReceived(message: String) {

    val commandFromMessage = message.split(" ")[0]

    logic.commands.filterKeys { it.matches("$commandFromMessage( |$)".toRegex()) }
        .values.firstOrNull()?.invoke(ChatMessage(messagePlain = message))
        ?: IllegalArgumentException("Command not known")
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
    verify(delegateMock, never()).setGoal(any())
  }

  private fun thenWebsocketIsClosed(expected: Boolean = true, delay: Boolean = false) {

    if (expected) {
      verify(delegateMock).closeConnection(delay)
    } else {
      verify(delegateMock, never()).closeConnection(any())
    }
  }

  private fun <T : RaceRoomLogic> thenLogicChangeIsRequested(expectedType: KClass<T>) {
    verify(delegateMock).changeLogic(expectedType)
  }

  private fun thenNoLogicChangeIsRequested() {
    verify(delegateMock, never()).changeLogic(any<KClass<RaceRoomLogic>>())
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private val messagesSent: List<String>
    get() =
      argumentCaptor<String>()
          .also { verify(delegateMock, atLeast(0)).sendMessage(it.capture(), any(), anyOrNull(), anyOrNull()) }
          .allValues

  private val goal: String
    get() =
      argumentCaptor<String>()
          .also { verify(delegateMock).setGoal(it.capture()) }
          .lastValue

  //</editor-fold>
}
