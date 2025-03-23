package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant.RacetimeEntrantStatus.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.AntiBingoState.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*

class RaceOpenStageTest {

  //<editor-fold desc="Setup">

  private val entrantPairGeneratorMock = mock<EntrantPairGenerator>()
  private val completeStageMock = mock<(AntiBingoState) -> Unit>()

  private val sentDms = mutableListOf<Pair<String, RacetimeUser?>>()

  private val stage = RaceOpenStage(entrantPairGeneratorMock, completeStageMock) { message, user ->
    sentDms.addLast(message to user)
  }

  //</editor-fold>

  @Test
  internal fun doesNotCompleteIfNoEntrants() {

    whenRaceUpdateIsReceived()

    thenStageCompletionIsNotCalled()
  }

  @Test
  internal fun doesNotCompleteIfOneEntrants() {

    whenRaceUpdateIsReceived(entrant(READY))

    thenStageCompletionIsNotCalled()
  }

  @Test
  internal fun completesIfTwoEntrantsBothReady() {

    val (id1, id2) = (1..2).map { UUID.randomUUID().toString() }
    val (name1, name2) = (1..2).map { UUID.randomUUID().toString() }

    val entrant1 = entrant(id1, name1)
    val entrant2 = entrant(id2, name2)

    whenRaceUpdateIsReceived(entrant1, entrant2)

    thenStageCompletionIsCalledWithEntrants(entrant1.user, entrant2.user)
  }

  @Test
  internal fun doesNotCompleteIfTwoEntrantsOneNotReady() {

    whenRaceUpdateIsReceived(entrant(READY), entrant(NOT_READY))

    thenStageCompletionIsNotCalled()
  }

  @Test
  internal fun completesIfThreeEntrantsAllReady() {

    val (id1, id2, id3) = (1..3).map { UUID.randomUUID().toString() }
    val (name1, name2, name3) = (1..3).map { UUID.randomUUID().toString() }

    val entrant1 = entrant(id1, name1)
    val entrant2 = entrant(id2, name2)
    val entrant3 = entrant(id3, name3)

    val pairs = listOf(entrantMapping(entrant1.user, entrant3.user))

    givenPairGeneratorReturnsPairs(pairs)

    whenRaceUpdateIsReceived(entrant1, entrant2, entrant3)

    thenStageCompletionIsCalledWithEntrants(entrant1.user, entrant2.user, entrant3.user)
    thenStageCompletionIsCalledWithPairs(pairs)
  }

  @Test
  internal fun generatesPairsIfThreeEntrantsAreReady() {

    val (id1, id2, id3) = (1..3).map { UUID.randomUUID().toString() }
    val (name1, name2, name3) = (1..3).map { UUID.randomUUID().toString() }

    val entrant1 = entrant(id1, name1)
    val entrant2 = entrant(id2, name2)
    val entrant3 = entrant(id3, name3)

    whenRaceUpdateIsReceived(entrant1, entrant2, entrant3)

    thenPairGeneratorIsCalledWithEntrants(entrant1.user, entrant2.user, entrant3.user)
  }

  @Test
  internal fun dmsEntrantsIfThreeEntrantsAreReady() {

    val (id1, id2, id3) = (1..3).map { UUID.randomUUID().toString() }
    val (name1, name2, name3) = (1..3).map { UUID.randomUUID().toString() }

    val entrant1 = entrant(id1, name1)
    val entrant2 = entrant(id2, name2)
    val entrant3 = entrant(id3, name3)

    val pairs = listOf(
      entrantMapping(entrant1.user, entrant3.user),
      entrantMapping(entrant1.user, entrant1.user),
      entrantMapping(entrant2.user, entrant3.user),
      entrantMapping(entrant3.user, entrant2.user),
    )

    givenPairGeneratorReturnsPairs(pairs)

    whenRaceUpdateIsReceived(entrant1, entrant2, entrant3)

    thenDmsAreSent(
      name3 to entrant1,
      name1 to entrant1,
      name3 to entrant2,
      name2 to entrant3,
    )
  }

  @Test
  internal fun doesNotCompleteIfThreeEntrantsOneNotReady() {

    whenRaceUpdateIsReceived(entrant(READY), entrant(READY), entrant(NOT_READY))

    thenStageCompletionIsNotCalled()
  }

  //<editor-fold desc="Given">

  private fun givenPairGeneratorReturnsPairs(pairs: List<EntrantMapping>) {
    whenever(entrantPairGeneratorMock.generatePairs(any())).thenReturn(pairs)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenRaceUpdateIsReceived(vararg entrants: RacetimeEntrant) {
    stage.raceUpdate(RacetimeRace(entrants = entrants.toList()))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenStageCompletionIsNotCalled() {
    verifyNoInteractions(completeStageMock)
  }

  private fun thenStageCompletionIsCalledWithEntrants(vararg expectedEntrants: RacetimeUser?) {

    expectedEntrants.forEach { checkNotNull(it) }
    val captor = argumentCaptor<AntiBingoState>()
    verify(completeStageMock).invoke(captor.capture())
    val state = captor.lastValue

    assertThat(state.entrants).containsExactlyInAnyOrder(*expectedEntrants)
  }

  private fun thenStageCompletionIsCalledWithPairs(expectedPairs: List<EntrantMapping>) {

    val captor = argumentCaptor<AntiBingoState>()
    verify(completeStageMock).invoke(captor.capture())
    val state = captor.lastValue

    assertThat(state.entrantMappings).containsExactlyElementsOf(expectedPairs)
  }

  private fun thenPairGeneratorIsCalledWithEntrants(vararg expectedEntrants: RacetimeUser?) {

    expectedEntrants.forEach { checkNotNull(it) }
    val captor = argumentCaptor<List<RacetimeUser>>()
    verify(entrantPairGeneratorMock).generatePairs(captor.capture())
    assertThat(captor.firstValue).containsExactlyInAnyOrder(*expectedEntrants)
  }

  private fun thenDmsAreSent(vararg expectedDms: Pair<String, RacetimeEntrant>) {
    assertThat(sentDms).containsExactlyInAnyOrderElementsOf(
      expectedDms.map {
        it.first.let { name -> "Please choose a row for $name" } to it.second.user
      }
    )
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private fun entrant(status: RacetimeEntrant.RacetimeEntrantStatus) = RacetimeEntrant(status = status)

  private fun entrant(id: String, name: String) =
    RacetimeEntrant(user = RacetimeUser(id = id, name = name), status = READY)

  private fun entrantMapping(entrant: RacetimeUser?, choosesFor: RacetimeUser?) =
    EntrantMapping(checkNotNull(entrant), checkNotNull(choosesFor), null)

  //</editor-fold>
}
