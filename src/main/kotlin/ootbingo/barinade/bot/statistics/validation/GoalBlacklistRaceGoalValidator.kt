package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.extensions.containsAny
import org.springframework.stereotype.Service

@Service
class GoalBlacklistRaceGoalValidator {

  private val blacklistedWords = listOf(
      "short", "long", "blackout", "black out", "3x3", "anti", "double",
      "bufferless", "child", "jp", "japanese", "bingo-j"
  )

  fun validateGoal(goal: String): Boolean = !goal.lowercase().containsAny(blacklistedWords)
}
