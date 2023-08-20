package ootbingo.barinade.bot.statistics.validation

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DateRaceGoalValidatorTest {

  private val validator = DateRaceGoalValidator()

  @Test
  internal fun srlBingoBeforeGithubSwitch() {
    assertThat(validator.validateSrlBingoDate(date(2019, 9, 20))).isTrue()
  }

  @Test
  internal fun noSrlBingoAfterGithubSwitch() {
    assertThat(validator.validateSrlBingoDate(date(2019, 9, 21))).isFalse()
  }

  private fun date(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0) =
      ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.of("UTC")).toInstant()
}
