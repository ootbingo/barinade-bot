package ootbingo.barinade.bot.racing_services.data.model.helper

import ootbingo.barinade.bot.racing_services.data.model.ResultType
import java.time.Duration
import java.time.Instant

data class ResultInfo(
    val time: Duration?,
    val goal: String,
    val raceId: String,
    val datetime: Instant,
    val resultType: ResultType
)
