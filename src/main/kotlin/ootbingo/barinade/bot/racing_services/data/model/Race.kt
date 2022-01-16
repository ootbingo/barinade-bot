package ootbingo.barinade.bot.racing_services.data.model

import ootbingo.barinade.bot.extensions.containsAny
import ootbingo.barinade.bot.properties.BingoRaceProperties
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.persistence.*

@Entity
data class Race(
    @Id var raceId: String = "",
    var goal: String = "",
    var datetime: Instant = Instant.now(),
    @Enumerated(EnumType.STRING)
    var platform: Platform = Platform.SRL,
    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "resultId.race", fetch = FetchType.EAGER)
    var raceResults: MutableList<RaceResult> = mutableListOf(),
) {

  fun isBingo(): Boolean {

    val blacklistedWords = listOf("short", "long", "blackout", "black out", "3x3", "anti", "double", "bufferless",
        "child", "jp", "japanese", "bingo-j")

    if (this.raceId in BingoRaceProperties.blacklist) {
      return false
    }

    if (this.raceId in BingoRaceProperties.whitelist.map { it.raceId }) {
      return true
    }

    with(this.goal.toLowerCase()) {
      return when {

        containsAny(blacklistedWords) -> false

        contains("speedrunslive.com/tools/oot-bingo") ->
          this@Race.datetime.isBefore(ZonedDateTime.of(2019, 9, 21, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant())
        matches(Regex("https?://ootbingo\\.github\\.io/bingo/v\\d+\\.\\d/bingo\\.html.*")) -> true
        matches(Regex("https?://ootbingo\\.github\\.io/bingo/beta0\\.9\\.6\\.\\d-j/bingo\\.html.*")) -> true

        else -> false
      }
    }
  }
}
