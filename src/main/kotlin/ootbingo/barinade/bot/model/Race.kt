package ootbingo.barinade.bot.model

import ootbingo.barinade.bot.properties.BingoRaceProperties
import java.time.ZoneId
import java.time.ZonedDateTime

data class Race(val srlId: String, val goal: String, val recordDate: ZonedDateTime, val numberOfEntrants: Long,
                val raceResults: List<RaceResult>)

fun Race.isBingo(): Boolean {

  val blacklistedWords = listOf("short", "long", "blackout", "black out", "3x3", "anti", "double", "bufferless",
                                "child", "jp", "japanese", "bingo-j")

  if (this.srlId.toInt() in BingoRaceProperties.blacklist) {
    return false
  }

  with(this.goal.toLowerCase()) {
    return when {

      containsAny(blacklistedWords) -> false

      contains("speedrunslive.com/tools/oot-bingo") ->
        this@isBingo.recordDate.isBefore(ZonedDateTime.of(2019, 9, 21, 0, 0, 0, 0, ZoneId.of("UTC")))
      matches(Regex("https?://ootbingo\\.github\\.io/bingo/v\\d+\\.\\d/bingo\\.html.*")) -> true

      else -> false
    }
  }
}

private fun String.containsAny(patterns: Collection<String>): Boolean {
  return patterns.any { it in this }
}
