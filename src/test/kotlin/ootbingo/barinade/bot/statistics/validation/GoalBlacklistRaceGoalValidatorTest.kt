package ootbingo.barinade.bot.statistics.validation

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

class GoalBlacklistRaceGoalValidatorTest {

  private val validator = GoalBlacklistRaceGoalValidator()

  @Test
  internal fun bingoWhenGoalDoesNotContainBlacklistedWord() {
    assertThat(validator.validateGoal("abc ${UUID.randomUUID()} xyz")).isTrue()
  }

  @ParameterizedTest
  @ValueSource(strings = ["short", "long", "blackout", "black out", "3x3",
    "anti", "double", "bufferless", "child", "jp", "japanese", "bingo-j"])
  internal fun noBingoWhenGoalContainsBlacklistedWord(word: String) {
    assertThat(validator.validateGoal("abc $word xyz")).isFalse()
  }

  @ParameterizedTest
  @ValueSource(strings = ["SHORT", "loNG", "BLACKout", "Japanese", "bInGo-J"])
  internal fun noBingoWhenGoalContainsBlacklistedWordWithCapitalizedLetters(word: String) {
    assertThat(validator.validateGoal("abc $word xyz")).isFalse()
  }
}
