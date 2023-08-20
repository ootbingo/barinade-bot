package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.extensions.containsAny
import ootbingo.barinade.bot.properties.BingoRaceProperties
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class RaceGoalValidator {

  private val blacklistedWords = listOf(
      "short", "long", "blackout", "black out", "3x3", "anti", "double",
      "bufferless", "child", "jp", "japanese", "bingo-j"
  )

  fun isBingo(id: String, goal: String, date: Instant): Boolean {
    if (id in BingoRaceProperties.blacklist) {
      return false
    }

    if (id in BingoRaceProperties.whitelist.map { it.raceId }) {
      return true
    }

    with(goal.lowercase()) {
      return when {

        containsAny(blacklistedWords) -> false

        contains("speedrunslive.com/tools/oot-bingo") ->
          date.isBefore(ZonedDateTime.of(2019, 9, 21, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant())

        matches(Regex("https?://ootbingo\\.github\\.io/bingo/v\\d+\\.\\d/bingo\\.html.*")) -> true
        matches(Regex("https?://ootbingo\\.github\\.io/bingo/beta0\\.9\\.6\\.\\d-j/bingo\\.html.*")) -> true

        else -> false
      }
    }
  }
}
