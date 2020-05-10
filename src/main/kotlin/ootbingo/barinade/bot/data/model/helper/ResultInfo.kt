package ootbingo.barinade.bot.data.model.helper

import ootbingo.barinade.bot.data.model.Race
import java.time.Duration
import java.time.ZonedDateTime

data class ResultInfo(val time: Duration, val goal: String, val raceId: String, val recordDate: ZonedDateTime) {

  val isBingo
    get() = Race("0", goal, recordDate, 0, mutableListOf()).isBingo()

  val isForfeit
    get() = time.isNegative
}
