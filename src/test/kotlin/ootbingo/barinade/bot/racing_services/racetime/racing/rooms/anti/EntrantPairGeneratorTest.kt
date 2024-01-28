package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

class EntrantPairGeneratorTest {

  //<editor-fold desc="Setup">

  private val generator = EntrantPairGenerator()

  private lateinit var generatedPairs: List<AntiBingoState.EntrantMapping>
  private lateinit var caughtException: Exception

  //</editor-fold>

  @Test
  internal fun throwsIfEntrantListIsEmpty() {

    whenPairsAreGenerated(emptyList())

    thenExceptionIsThrown<IllegalArgumentException>()
  }

  @Test
  internal fun throwsIfEntrantListHasOneItem() {

    whenPairsAreGenerated(listOf(RacetimeUser()))

    thenExceptionIsThrown<IllegalArgumentException>()
  }

  @Test
  internal fun matchesUpTwoEntrants() {

    val (user1, user2) = (1..2).map { racetimeUser() }

    whenPairsAreGenerated(listOf(user1, user2))

    thenPairsAreGenerated(
        mapping(user1, user2),
        mapping(user2, user1),
    )
  }

  @Test
  internal fun matchesUpBiggerLists() {

    repeat(100) {

      val numberOfEntrants = Random.nextInt(3, 10)
      val entrants = (1..numberOfEntrants).map { racetimeUser() }

      whenPairsAreGenerated(entrants)

      val soft = SoftAssertions()

      soft.assertThat(generatedPairs).describedAs("Wrong number of pairs").hasSize(entrants.size)

      soft.assertThat(generatedPairs.none { it.entrant == it.choosesFor })
          .describedAs("Entrant paired with themselves")
          .isTrue()

      val mappingFroms = generatedPairs.map { it.entrant }
      val mappingTos = generatedPairs.map { it.entrant }

      entrants.forEach {
        soft.assertThat(mappingFroms).describedAs("No partner for entrant").contains(it)
        soft.assertThat(mappingTos).describedAs("No picker for entrant").contains(it)
      }

      soft.assertAll()
    }
  }

  //<editor-fold desc="When">

  private fun whenPairsAreGenerated(entrants: List<RacetimeUser>) {
    try {
      generatedPairs = generator.generatePairs(entrants)
    } catch (e: Exception) {
      caughtException = e
    }
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenPairsAreGenerated(vararg expectedPairs: AntiBingoState.EntrantMapping) {
    assertThat(generatedPairs).containsExactlyInAnyOrder(*expectedPairs)
  }

  private inline fun <reified T : Exception> thenExceptionIsThrown() {
    assertThat(caughtException).isInstanceOf(T::class.java)
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private fun racetimeUser() = RacetimeUser(id = UUID.randomUUID().toString())

  private fun mapping(entrant: RacetimeUser, choosesFor: RacetimeUser) =
      AntiBingoState.EntrantMapping(entrant, choosesFor, null)

  //</editor-fold>
}
