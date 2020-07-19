package ootbingo.barinade.bot.data.model.helper

import ootbingo.barinade.bot.data.model.ResultType
import java.time.Duration
import java.time.ZonedDateTime

data class ResultInfo(
    val time: Duration,
    val goal: String,
    val raceId: String,
    val datetime: ZonedDateTime,
    val resultType: ResultType
)
