package ootbingo.barinade.bot.racing_services.data

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

internal class UsernameMapperTest {

  private var mapper: UsernameMapper? = null
  private var result: String? = null

  //<editor-fold desc="SRL to Racetime">

  @Test
  internal fun mapsSrlNameToRacetimeName() {

    givenMapping(
        """
          #Comment
          racetime;SRL
          SRL;blub
        """
    )

    whenRacetimeNameIsQueriedForSrlName("SRL")

    thenResultIs("racetime")
  }

  @Test
  internal fun mapsSrlNameToRacetimeNameCaseInsensitive() {

    givenMapping("rtgg;SRL")

    whenRacetimeNameIsQueriedForSrlName("srl")

    thenResultIs("rtgg")
  }

  @Test
  internal fun returnsSrlNameIfNoMappingFound() {

    val srlName = UUID.randomUUID().toString()

    givenMapping("")

    whenRacetimeNameIsQueriedForSrlName(srlName)

    thenResultIs(srlName)
  }

  //</editor-fold>

  //<editor-fold desc="Racetime to SRL">

  @Test
  internal fun mapsRacetimeNameToSrlName() {

    givenMapping(
        """
          #Comment
          racetime;SRL
          SRL;blub
        """
    )

    whenSrlNameIsQueriedForRacetimeName("racetime")

    thenResultIs("SRL")
  }

  @Test
  internal fun mapsRacetimeNameToSrlNameCaseInsensitive() {

    givenMapping("rtgg;SRL")

    whenSrlNameIsQueriedForRacetimeName("RTGG")

    thenResultIs("SRL")
  }

  @Test
  internal fun returnsRacetimeNameIfNoMappingFound() {

    val racetimeName = UUID.randomUUID().toString()

    givenMapping("")

    whenSrlNameIsQueriedForRacetimeName(racetimeName)

    thenResultIs(racetimeName)
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenMapping(csv: String) {
    mapper = UsernameMapper(csv)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenRacetimeNameIsQueriedForSrlName(srlName: String) {
    result = mapper?.srlToRacetime(srlName)
  }

  private fun whenSrlNameIsQueriedForRacetimeName(racetimeName: String) {
    result = mapper?.racetimeToSrl(racetimeName)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenResultIs(expectedResult: String) {
    assertThat(result).isEqualTo(expectedResult)
  }

  //</editor-fold>
}
