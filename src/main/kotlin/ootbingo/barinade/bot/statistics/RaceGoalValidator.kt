package ootbingo.barinade.bot.statistics

import ootbingo.barinade.bot.racing_services.data.model.Race
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RaceGoalValidator {

  private val blacklistedWords = listOf(
      "short", "long", "blackout", "black out", "3x3", "anti", "double",
      "bufferless", "child", "jp", "japanese", "bingo-j"
  )

  fun isBingo(id: String, goal: String, date: Instant): Boolean {
    return Race(raceId = id, goal = goal, datetime = date).isBingo()
  }
}
