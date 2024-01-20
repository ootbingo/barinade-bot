package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.AntiBingoStage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.PreRaceStage
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.*

class AntiBingoRaceRoomLogicTest {

  //<editor-fold desc="Setup">

  private val statusHolder = RaceStatusHolder()
  private val stageHolder = Holder<AntiBingoStage>(PreRaceStage)
  private val racetimeHttpClientMock = mock<RacetimeHttpClient>()
  private val delegateMock = mock<RaceRoomDelegate>()

  private var stage by stageHolder

  private val logic = AntiBingoRaceRoomLogic(statusHolder, stageHolder, racetimeHttpClientMock, delegateMock)

  //</editor-fold>

  //<editor-fold desc="Test: initialize">

  @Test
  internal fun editsRaceDuringInitialization() {

    val slug = UUID.randomUUID().toString()
    val race = RacetimeRace(slug = slug)

    whenRaceIsInitialized(race)

    thenRace(slug).isSetToManualStart()
  }

  @Test
  internal fun sendsMessageDuringInitialization() {

    whenRaceIsInitialized()

    thenMessageIsSent("Anti-Bingo initialized")
  }

  @Test
  internal fun persistsRaceDuringInitialization() {

    val race = RacetimeRace(name = UUID.randomUUID().toString())

    whenRaceIsInitialized(race)

    thenRaceIsPersisted(race)
  }

  //</editor-fold>

  //<editor-fold desc="Test: onRaceUpdate">

  @Test
  internal fun persistRaceOnUpdate() {

    val race = RacetimeRace(name = UUID.randomUUID().toString())

    whenRaceUpdateIsReceived(race)

    thenRaceIsPersisted(race)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenRaceIsInitialized(race: RacetimeRace = RacetimeRace()) {
    logic.initialize(race)
  }

  private fun whenRaceUpdateIsReceived(race: RacetimeRace) {
    logic.onRaceUpdate(race)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenMessageIsSent(expectedMessage: String) {
    verify(delegateMock).sendMessage(expectedMessage, false, null)
  }

  private fun thenRaceIsPersisted(expectedRace: RacetimeRace) {
    assertThat(statusHolder.race).isEqualTo(expectedRace)
  }

  private fun thenRace(slug: String) = RaceSlug(slug)

  private fun RaceSlug.isSetToManualStart() {

    val captor = argumentCaptor<RacetimeEditableRace.() -> Unit>()
    verify(racetimeHttpClientMock).editRace(eq(this.slug), captor.capture())
    val edits = captor.lastValue

    val testRace = mock<RacetimeEditableRace>()
    edits.invoke(testRace)
    verify(testRace).autoStart = false
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  @JvmInline
  value class RaceSlug(val slug: String)

  //</editor-fold>
}
