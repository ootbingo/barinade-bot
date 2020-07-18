package ootbingo.barinade.bot.data.model

import ootbingo.barinade.bot.compile.Open
import ootbingo.barinade.bot.extensions.containsAny
import ootbingo.barinade.bot.properties.BingoRaceProperties
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
@Open
data class Race(@Id var raceId: String = "",
                var goal: String = "",
                var datetime: ZonedDateTime = ZonedDateTime.now(),
                @Enumerated(EnumType.STRING)
                var platform: Platform = Platform.SRL,
                @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "resultId.race", fetch = FetchType.EAGER)
                var raceResults: MutableList<RaceResult> = mutableListOf()) {

  fun isBingo(): Boolean {

    val blacklistedWords = listOf("short", "long", "blackout", "black out", "3x3", "anti", "double", "bufferless",
                                  "child", "jp", "japanese", "bingo-j")

    if (this.raceId.toInt() in BingoRaceProperties.blacklist) {
      return false
    }

    if (this.raceId.toInt() in BingoRaceProperties.whitelist.map { it.raceId }) {
      return true
    }

    with(this.goal.toLowerCase()) {
      return when {

        containsAny(blacklistedWords) -> false

        contains("speedrunslive.com/tools/oot-bingo") ->
          this@Race.datetime.isBefore(ZonedDateTime.of(2019, 9, 21, 0, 0, 0, 0, ZoneId.of("UTC")))
        matches(Regex("https?://ootbingo\\.github\\.io/bingo/v\\d+\\.\\d/bingo\\.html.*")) -> true

        else -> false
      }
    }
  }
}
