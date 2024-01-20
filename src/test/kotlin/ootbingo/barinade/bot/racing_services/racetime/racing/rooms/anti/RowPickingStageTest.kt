package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeActionButton
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.AntiBingoState.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RowPickingStageTest {

  //<editor-fold desc="Setup">

  private val completeStageMock = mock<(AntiBingoState) -> Unit>()
  private val stateHolder = Holder(AntiBingoState(emptyList(), emptyList()))
  private val editRaceMock = mock<(RacetimeEditableRace.() -> Unit) -> Unit>()
  private val sendMessageMock = mock<(String, Map<String, RacetimeActionButton>?) -> Unit>()
  private val kickUserMock = mock<(RacetimeUser) -> Unit>()

  private var state by stateHolder

  private val stage = RowPickingStage(completeStageMock, stateHolder, editRaceMock, sendMessageMock, kickUserMock)

  //</editor-fold>

  //<editor-fold desc="Test: Initialization">

  @Test
  internal fun persistsStateWhileInitializing() {

    val initialState = defaultTwoPlayerState()

    givenState(AntiBingoState(listOf(), listOf()))

    whenStageIsInitialized(initialState)

    thenState isEqualTo initialState
  }

  @Test
  internal fun setsDelayWhileInitializing() {

    whenStageIsInitialized()

    thenChatDelayIsSetTo(90.seconds)
  }

  @Test
  internal fun sendMessageWhileInitializing() {

    whenStageIsInitialized()

    thenMessageMatches(".*You have 5 minutes to pick a row\\..*".toRegex())
  }

  //</editor-fold>

  //<editor-fold desc="Test: Command">

  @ParameterizedTest
  @EnumSource(Row::class)
  internal fun addsNewRowChoice(row: Row) {

    val (entrant1, entrant2, entrant3) = entrants()

    givenState(
        state(
            EntrantMapping(entrant1, entrant2, null),
            EntrantMapping(entrant2, entrant3, null),
            EntrantMapping(entrant3, entrant1, Row.TLBR),
        )
    )

    whenMessageIsReceived("!pick $row", entrant1)

    thenState containsChosenMapping EntrantMapping(entrant1, entrant2, row)
    thenState containsChosenMapping EntrantMapping(entrant2, entrant3, null)
    thenState containsChosenMapping EntrantMapping(entrant3, entrant1, Row.TLBR)
  }

  @Test
  internal fun doesNothingIfSenderNull() {

    val initialState = defaultTwoPlayerState()

    givenState(initialState)

    whenMessageIsReceived("!pick COL4", null)

    thenState isEqualTo initialState
  }

  @Test
  internal fun doesNothingIfSenderNotAnEntrant() {

    val (entrant1, entrant2) = entrants()

    val initialState = state(EntrantMapping(entrant1, entrant2, null))

    givenState(initialState)

    whenMessageIsReceived("!pick COL4", entrant2)

    thenState isEqualTo initialState
  }

  @Test
  internal fun doesNothingIfRowInvalid() {

    val initialState = defaultTwoPlayerState()

    givenState(initialState)

    whenMessageIsReceived("!pick COL6", null)

    thenState isEqualTo initialState
  }

  @Test
  internal fun doesNothingIfCommandUnknown() {

    val initialState = defaultTwoPlayerState()

    givenState(initialState)

    whenMessageIsReceived("!edit COL4", null)

    thenState isEqualTo initialState
  }

  //</editor-fold>

  //<editor-fold desc="Test: Race Update">

  @Test
  internal fun kicksUsersThatJoinDuringStage() {

    val (entrant1, entrant2, newUser1, newUser2) = entrants()

    givenState(AntiBingoState(listOf(entrant1, entrant2), emptyList()))

    whenRaceUpdateIsReceived(RacetimeRace(
        entrants = listOf(entrant1, entrant2, newUser1, newUser2).map { RacetimeEntrant(user = it) }
    ))

    thenMessageMatches((".*No new entrants permitted\\.").toRegex())
    thenUsersAreKicked(newUser1, newUser2)
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenState(state: AntiBingoState) {
    this.state = state
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenStageIsInitialized(
      initialState: AntiBingoState = AntiBingoState(emptyList(), emptyList()),
      race: RacetimeRace = RacetimeRace(),
  ) {
    stage.initialize(initialState, race)
  }

  private fun whenRaceUpdateIsReceived(race: RacetimeRace) {
    stage.raceUpdate(race)
  }

  private fun whenMessageIsReceived(command: String, user: RacetimeUser? = RacetimeUser()) {
    stage.handleCommand(ChatMessage(messagePlain = command, user = user))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenChatDelayIsSetTo(expectedDuration: Duration) {
    val captor = argumentCaptor<RacetimeEditableRace.() -> Unit>()
    verify(editRaceMock).invoke(captor.capture())
    val edits = captor.firstValue

    val testRace = mock<RacetimeEditableRace>()
    edits.invoke(testRace)
    verify(testRace).chatMessageDelay = expectedDuration.inWholeSeconds.toInt()
  }

  private fun thenMessageMatches(messageRegex: Regex) {
    val captor = argumentCaptor<String>()
    verify(sendMessageMock, atLeastOnce()).invoke(captor.capture(), anyOrNull())
    assertThat(captor.allValues).anyMatch { it.matches(messageRegex) }
  }

  private fun thenUsersAreKicked(vararg expectedUsers: RacetimeUser) {
    val captor = argumentCaptor<RacetimeUser>()
    verify(kickUserMock, times(expectedUsers.size)).invoke(captor.capture())
    assertThat(captor.allValues).containsExactlyInAnyOrder(*expectedUsers)
  }

  private val thenState
    get() = state

  private infix fun AntiBingoState.containsChosenMapping(expectedMapping: EntrantMapping) {
    assertThat(this.entrantMappings).contains(expectedMapping)
  }

  private infix fun AntiBingoState.isEqualTo(expectedState: AntiBingoState) {
    assertThat(this).isEqualTo(expectedState)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private fun entrantUser() = RacetimeUser(name = UUID.randomUUID().toString())

  private fun entrants() = (1..10).map { entrantUser() }

  private fun state(vararg entrantMappings: EntrantMapping) =
      AntiBingoState(entrantMappings.flatMap { listOf(it.entrant, it.choosesFor) }, entrantMappings.asList())

  private fun defaultTwoPlayerState(): AntiBingoState {
    val (entrant1, entrant2) = entrants()

    val initialState = state(
        EntrantMapping(entrant1, entrant2, null),
        EntrantMapping(entrant2, entrant1, null),
    )
    return initialState
  }

  //</editor-fold>
}
