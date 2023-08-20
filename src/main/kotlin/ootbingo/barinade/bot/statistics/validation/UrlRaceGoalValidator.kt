package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.statistics.validation.UrlRaceGoalValidator.GoalType.*
import org.springframework.stereotype.Service

@Service
class UrlRaceGoalValidator {

  fun validateGoal(goal: String): GoalType {
    with(goal.lowercase()) {
      return when {

        contains("speedrunslive.com/tools/oot-bingo") -> SRL_BINGO

        matches(Regex("https?://ootbingo\\.github\\.io/bingo/v\\d+\\.\\d/bingo\\.html.*")) -> GITHUB_IO_LEGACY_BINGO
        matches(Regex("https?://ootbingo\\.github\\.io/bingo/beta0\\.9\\.6\\.\\d-j/bingo\\.html.*")) -> GITHUB_IO_LEGACY_BINGO

        else -> NO_BINGO
      }
    }
  }

  enum class GoalType {
    NO_BINGO,
    SRL_BINGO,
    GITHUB_IO_LEGACY_BINGO,
    GITHUB_IO_BINGO
  }
}
