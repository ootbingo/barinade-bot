package ootbingo.barinade.bot.model

import ootbingo.barinade.bot.extensions.containsAny
import ootbingo.barinade.bot.properties.BingoRaceProperties
import java.time.ZoneId
import java.time.ZonedDateTime

open class Race(val srlId: String, val goal: String, val recordDate: ZonedDateTime, val numberOfEntrants: Long,
                val raceResults: List<RaceResult>) {

  open fun isBingo(): Boolean {

    val blacklistedWords = listOf("short", "long", "blackout", "black out", "3x3", "anti", "double", "bufferless",
                                  "child", "jp", "japanese", "bingo-j")

    if (this.srlId.toInt() in BingoRaceProperties.blacklist) {
      return false
    }

    if (this.srlId.toInt() in BingoRaceProperties.whitelist.map { it.raceId }) {
      return true
    }

    with(this.goal.toLowerCase()) {
      return when {

        containsAny(blacklistedWords) -> false

        contains("speedrunslive.com/tools/oot-bingo") ->
          this@Race.recordDate.isBefore(ZonedDateTime.of(2019, 9, 21, 0, 0, 0, 0, ZoneId.of("UTC")))
        matches(Regex("https?://ootbingo\\.github\\.io/bingo/v\\d+\\.\\d/bingo\\.html.*")) -> true

        else -> false
      }
    }
  }
}
