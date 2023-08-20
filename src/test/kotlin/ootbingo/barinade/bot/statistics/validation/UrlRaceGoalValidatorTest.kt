package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.statistics.validation.UrlRaceGoalValidator.GoalType.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class UrlRaceGoalValidatorTest {

  private val validator = UrlRaceGoalValidator()

  //<editor-fold desc="Test: SRL">

  @ParameterizedTest
  @ValueSource(strings = ["", "www."])
  internal fun isBingoWhenSrlUrl(prefix: String) {
    "http://${prefix}speedrunslive.com/tools/oot-bingo/?seed=257318&mode=normal" isGoalType SRL_BINGO
  }

  @ParameterizedTest
  @ValueSource(strings = ["", "www."])
  internal fun isBingoWhenSrlUrlWithVersion(prefix: String) {
    "http://${prefix}speedrunslive.com/tools/oot-bingo-v4/?seed=273307" isGoalType SRL_BINGO
  }

  //</editor-fold>

  //<editor-fold desc="Test: github.io Legacy">

  @Test
  internal fun isBingoWhenGithubIoUrl() {
    "https://ootbingo.github.io/bingo/v9.4/bingo.html?seed=860838&mode=normal" isGoalType GITHUB_IO_LEGACY_BINGO
  }

  @Test
  internal fun isBingoWhenJpBetaUrl() {
    "https://ootbingo.github.io/bingo/beta0.9.6.2-j/bingo.html?seed=424242&mode=normal" isGoalType GITHUB_IO_LEGACY_BINGO
  }

  @ParameterizedTest
  @ValueSource(strings = ["0.9.6.2", "0.9.5.0-j", "0.9.7.0-j"])
  internal fun noBingoWhenOtherBeta(beta: String) {
    "https://ootbingo.github.io/bingo/beta$beta/bingo.html?seed=860838&mode=normal" isGoalType NO_BINGO
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

  private infix fun String.isGoalType(expectedGoalType: UrlRaceGoalValidator.GoalType) {
    assertThat(validator.validateGoal(this)).isEqualTo(expectedGoalType)
  }

  //</editor-fold>
}
