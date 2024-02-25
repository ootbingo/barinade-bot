package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RaceStartedStageTest {

  //<editor-fold desc="Setup">

  private val editRaceMock = mock<(RacetimeEditableRace.() -> Unit) -> Unit>()

  private val sentMessages = mutableListOf<Pair<String, RacetimeUser?>>()

  private val stage = RaceStartedStage(mock(), editRaceMock, mock()) { message, user ->
    sentMessages.addLast(message to user)
  }

  //</editor-fold>

  //<editor-fold desc="Test: Race Settings">

  @Test
  internal fun resetsChatDelayAndAutostart() {

    whenStageIsInitialized()

    thenChatDelayIsSetTo(0.seconds)
    thenRaceIsSetToAutomaticStart()
  }

  //</editor-fold>

  //<editor-fold desc="Test: DM Rows">

  @Test
  internal fun dmsRowsWhenRaceStarts() {

    val (id1, id2, id3) = (1..3).map { UUID.randomUUID().toString() }

    whenStageIsInitialized(
      antiBingoState(
        entrantMappings = listOf(
          AntiBingoState.EntrantMapping(RacetimeUser(), RacetimeUser(id = id1), AntiBingoState.Row.COL4),
          AntiBingoState.EntrantMapping(RacetimeUser(), RacetimeUser(id = id3), AntiBingoState.Row.ROW2),
          AntiBingoState.EntrantMapping(RacetimeUser(), RacetimeUser(id = id2), AntiBingoState.Row.TLBR),
          AntiBingoState.EntrantMapping(RacetimeUser(), RacetimeUser(id = id3), AntiBingoState.Row.COL1),
        )
      )
    )

    whenRaceIsStarted()

    thenRowDmsAreSent(
      AntiBingoState.Row.COL4 to id1,
      AntiBingoState.Row.ROW2 to id3,
      AntiBingoState.Row.TLBR to id2,
      AntiBingoState.Row.COL1 to id3,
    )
  }

  @Test
  internal fun doesNotDmWhenRaceDidNotStartYet() {

    val (id1, id2) = (1..2).map { UUID.randomUUID().toString() }

    whenStageIsInitialized(
      antiBingoState(
        entrantMappings = listOf(
          AntiBingoState.EntrantMapping(RacetimeUser(), RacetimeUser(id = id1), AntiBingoState.Row.COL4),
          AntiBingoState.EntrantMapping(RacetimeUser(), RacetimeUser(id = id2), AntiBingoState.Row.ROW2),
        )
      )
    )

    whenRaceIsUpdated(RacetimeRace.RacetimeRaceStatus.PENDING)

    thenNumberOfDmsIsEqualTo(0)
  }

  @Test
  internal fun onlyDmsOnce() {

    val (id1, id2) = (1..2).map { UUID.randomUUID().toString() }

    whenStageIsInitialized(
      antiBingoState(
        entrantMappings = listOf(
          AntiBingoState.EntrantMapping(RacetimeUser(), RacetimeUser(id = id1), AntiBingoState.Row.COL4),
          AntiBingoState.EntrantMapping(RacetimeUser(), RacetimeUser(id = id2), AntiBingoState.Row.ROW2),
        )
      )
    )

    whenRaceIsStarted()
    whenRaceIsUpdated(RacetimeRace.RacetimeRaceStatus.IN_PROGRESS)

    thenNumberOfDmsIsEqualTo(2)
  }

  //</editor-fold>

  //<editor-fold desc="Test: Filename">

  @Test
  internal fun sendsFilenameWhenRaceStarts() {

    whenStageIsInitialized()
    whenRaceIsStarted()

    thenNumberOfMessageMatches(1, "^Filename: [A-Z]{2}$".toRegex())
  }

  @Test
  internal fun doesNotSendFilenameIfRaceHasNotStartedYet() {

    whenStageIsInitialized()
    whenRaceIsUpdated(RacetimeRace.RacetimeRaceStatus.PENDING)

    thenNumberOfMessageMatches(0, "^Filename: [A-Z]{2}$".toRegex())
  }

  @Test
  internal fun onlySendsFilenameOnce() {

    whenStageIsInitialized()
    whenRaceIsStarted()
    whenRaceIsUpdated(RacetimeRace.RacetimeRaceStatus.IN_PROGRESS)

    thenNumberOfMessageMatches(1, "^Filename: [A-Z]{2}$".toRegex())
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenStageIsInitialized(state: AntiBingoState = antiBingoState()) {
    stage.initialize(state, RacetimeRace())
  }

  private fun whenRaceIsUpdated(status: RacetimeRace.RacetimeRaceStatus) {
    stage.raceUpdate(RacetimeRace(status = status))
  }

  private fun whenRaceIsStarted() {
    whenRaceIsUpdated(RacetimeRace.RacetimeRaceStatus.IN_PROGRESS)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenRaceIsSetToAutomaticStart() {

    val captor = argumentCaptor<RacetimeEditableRace.() -> Unit>()
    verify(editRaceMock).invoke(captor.capture())
    val edits = captor.lastValue

    val testRace = mock<RacetimeEditableRace>()
    edits.invoke(testRace)
    verify(testRace).autoStart = true
  }

  private fun thenChatDelayIsSetTo(expectedDuration: Duration) {
    val captor = argumentCaptor<RacetimeEditableRace.() -> Unit>()
    verify(editRaceMock).invoke(captor.capture())
    val edits = captor.firstValue

    val testRace = mock<RacetimeEditableRace>()
    edits.invoke(testRace)
    verify(testRace).chatMessageDelay = expectedDuration.inWholeSeconds.toInt()
  }

  private fun thenRowDmsAreSent(vararg expectedDms: Pair<AntiBingoState.Row, String>) {
    assertThat(
      sentMessages.filter { it.second != null }.map { it.first to it.second?.id }
    ).containsExactlyInAnyOrderElementsOf(
      expectedDms.map { it.first.let { row -> "Your row is ${row.formatted}" } to it.second }
    )
  }

  private fun thenNumberOfDmsIsEqualTo(expectedNumberOfDms: Int) {
    assertThat(sentMessages.filter { it.second != null }).hasSize(expectedNumberOfDms)
  }

  private fun thenNumberOfMessageMatches(expectedNumberOfMatches: Int, pattern: Regex) {
    assertThat(
      sentMessages
        .filter { it.second == null }
        .map { it.first }
        .filter { it.matches(pattern) }
    ).hasSize(expectedNumberOfMatches)
  }

  //</editor-fold>
}
