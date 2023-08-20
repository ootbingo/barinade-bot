package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.statistics.validation.IdBlacklistRaceGoalValidator.IdType.*
import ootbingo.barinade.bot.statistics.validation.UrlRaceGoalValidator.GoalType.*
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RaceGoalValidator(
    private val urlValidator: UrlRaceGoalValidator,
    private val dateValidator: DateRaceGoalValidator,
    private val goalBlacklistValidator: GoalBlacklistRaceGoalValidator,
    private val idBlacklistValidator: IdBlacklistRaceGoalValidator,
) {

  fun isBingo(id: String, goal: String, date: Instant): Boolean {

    when (idBlacklistValidator.validateRaceId(id)) {
      BLACKLISTED -> return false
      WHITELISTED -> return true
      NEUTRAL -> {}
    }

    if (!goalBlacklistValidator.validateGoal(goal)) {
      return false
    }

    val urlType = urlValidator.validateGoal(goal)

    return when (urlType) {
      NO_BINGO -> false
      SRL_BINGO -> dateValidator.validateSrlBingoDate(date)
      else -> true
    }
  }
}
