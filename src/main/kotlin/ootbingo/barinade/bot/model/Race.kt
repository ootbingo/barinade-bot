package ootbingo.barinade.bot.model

import java.time.ZonedDateTime

data class Race(val srlId: String, val goal:String, val recordDate: ZonedDateTime, val numberOfEntrants: Long, val raceResults: List<RaceResult>)
